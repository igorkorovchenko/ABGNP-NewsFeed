package com.example.android.newsfeed.activities;

import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.example.android.newsfeed.R;
import com.example.android.newsfeed.loaders.SectionsLoader;
import com.example.android.newsfeed.models.SectionModel;
import com.example.android.newsfeed.models.Sections;
import com.example.android.newsfeed.views.DynamicListPreference;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity
                              implements LoaderManager.LoaderCallbacks<List<SectionModel>>,
                                         SectionsLoader.OnSectionsLoader{

    private Boolean isSubOptions = false;

    private static Preference.OnPreferenceChangeListener
            sBindPreferenceSummaryToValueListener = (preference, value) -> {
        String stringValue = value.toString();

        if (preference instanceof DynamicListPreference) {
            DynamicListPreference listPreference = (DynamicListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);
            preference.setSummary(
                    index >= 0
                            ? listPreference.getEntries()[index]
                            : null);
        } else {
            preference.setSummary(stringValue);
        }
        return true;
    };

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if ((id == android.R.id.home) && (!isSubOptions)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    protected boolean isValidFragment(String fragmentName) {
        isSubOptions = DataFilterPreferenceFragment.class.getName().equals(fragmentName);
        return PreferenceFragment.class.getName().equals(fragmentName)
                || DataFilterPreferenceFragment.class.getName().equals(fragmentName);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataFilterPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_filter);
            setHasOptionsMenu(true);
            bindPreferenceSummaryToValue(findPreference("pref_section"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public Loader<List<SectionModel>> onCreateLoader(int id, Bundle args) {
        Loader<List<SectionModel>> loader = null;
        if (id == SectionsLoader.LOADER_ID) {
            loader = new SectionsLoader(this, this);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<SectionModel>> loader, List<SectionModel> data) {
    }

    @Override
    public void onLoaderReset(Loader<List<SectionModel>> loader) {
    }

    @Override
    public void onSectionsLoadingFinished(List<SectionModel> sectionsList) {
        updateSectionsList(sectionsList);
    }

    private void updateSectionsList(List<SectionModel> sectionsList) {
        Sections.getInstance().addItems(sectionsList);
    }

    private void updateData() {
        if (Sections.getInstance().isEmpty()) {
            ConnectivityManager cm =
                    (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            assert cm != null;
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            if (isConnected) {
                getLoaderManager()
                        .restartLoader(SectionsLoader.LOADER_ID, null, this)
                        .forceLoad();
            }
        }
    }
}
