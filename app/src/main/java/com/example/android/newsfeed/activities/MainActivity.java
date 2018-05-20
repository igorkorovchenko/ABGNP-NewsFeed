package com.example.android.newsfeed.activities;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.newsfeed.R;
import com.example.android.newsfeed.controllers.NewsListAdapter;
import com.example.android.newsfeed.loaders.NewsLoader;
import com.example.android.newsfeed.models.NewsModel;

import java.util.List;

/**
 * Main Activity
 *
 * @package com.example.android.newsfeed
 * (c) 2018, Igor Korovchenko.
 */
public class MainActivity extends AppCompatActivity
                          implements SwipeRefreshLayout.OnRefreshListener,
                                     SearchView.OnQueryTextListener,
                                     LoaderManager.LoaderCallbacks<List<NewsModel>>,
                                     NewsLoader.OnNewsLoader {

    private static final Integer REFRESH_DELAY = 2400;

    private SwipeRefreshLayout swipeLayout;
    private NewsListAdapter newsAdapter;
    private TextView emptyNewsList;
    private RecyclerView newsList;
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupSearch();
        setupSwipeRefresher();
        setupNewsList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateData(searchQuery);
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(() -> {
            updateData(searchQuery);
            swipeLayout.setRefreshing(false);
        }, REFRESH_DELAY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.main_menu) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<List<NewsModel>> onCreateLoader(int id, Bundle args) {
        Loader<List<NewsModel>> loader = null;
        if (id == NewsLoader.LOADER_ID) {
            loader = new NewsLoader(this, args, this);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<NewsModel>> loader, List<NewsModel> data) {
    }

    @Override
    public void onLoaderReset(Loader<List<NewsModel>> loader) {
    }

    @Override
    public void onNewsLoadingFinished(List<NewsModel> newsList) {
        updateNewsList(newsList);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchQuery = query;
        updateData(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        searchQuery = newText;
        return false;
    }

    private void setupSearch() {
        SearchView searchView = findViewById(R.id.search);
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(true);
        searchView.setSubmitButtonEnabled(true);
    }

    private void setupSwipeRefresher() {
        swipeLayout = findViewById(R.id.refresh);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(
                getResources().getColor(R.color.colorPrimaryDark),
                getResources().getColor(R.color.colorAccent),
                getResources().getColor(R.color.colorSecondary));
    }

    private void setupNewsList() {
        newsAdapter = new NewsListAdapter();
        newsList = findViewById(R.id.list);
        newsList.setLayoutManager(new LinearLayoutManager(this));
        newsList.setAdapter(newsAdapter);
        emptyNewsList = findViewById(R.id.empty_list);
        getLoaderManager().initLoader(NewsLoader.LOADER_ID, null, this).forceLoad();
    }

    private void updateNewsList(List<NewsModel> list) {
        newsAdapter.setItems(list);
        newsAdapter.notifyDataSetChanged();
        checkEmptyNewsList();
    }

    private void updateData(String query) {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            Bundle bundle = new Bundle();
            bundle.putString(NewsLoader.ARGS_KEY_SEARCH, query);
            getLoaderManager().restartLoader(NewsLoader.LOADER_ID, bundle, this).forceLoad();
        }
    }

    private void checkEmptyNewsList() {
        if (newsAdapter.getItemCount() == 0) {
            newsList.setVisibility(View.GONE);
            emptyNewsList.setVisibility(View.VISIBLE);
        } else {
            newsList.setVisibility(View.VISIBLE);
            emptyNewsList.setVisibility(View.GONE);
        }
    }
}
