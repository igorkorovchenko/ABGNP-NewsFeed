package com.example.android.newsfeed.views;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.android.newsfeed.models.SectionModel;
import com.example.android.newsfeed.models.Sections;

import java.util.ArrayList;
import java.util.List;

/**
 * Class
 *
 * @package com.example.android.newsfeed.views
 * (c) 2018, Igor Korovchenko.
 */
public class DynamicListPreference extends ListPreference {

    private List<String> entries = new ArrayList<>();
    private List<String> entryValues = new ArrayList<>();

    public DynamicListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        getData();
    }

    public DynamicListPreference(Context context) {
        super(context);
        getData();
    }

    @Override
    protected View onCreateDialogView() {
        ListView view = new ListView(getContext());
        view.setAdapter(adapter());
        setEntries(entries());
        setEntryValues(entryValues());
        setValueIndex(0);
        return view;
    }

    private void setEntries(List<SectionModel> sectionModels) {
        for (SectionModel section:sectionModels) {
            entries.add(section.getTitle());
            entryValues.add(section.getId());
        }
    }

    private ListAdapter adapter() {
        return new ArrayAdapter(getContext(), android.R.layout.select_dialog_singlechoice);
    }

    private CharSequence[] entries() {
        return convertStringListToCharSequenceArray(entries);
    }

    private CharSequence[] entryValues() {
        return convertStringListToCharSequenceArray(entryValues);
    }

    private CharSequence[] convertStringListToCharSequenceArray(List<String> array) {
        Integer size = array.size();
        CharSequence[] result = new CharSequence[size];
        for (Integer i = 0; i < size; i++) {
            result[i] = array.get(i);
        }
        return result;
    }

    private void getData() {
        List<SectionModel> sections = new ArrayList<>(Sections.getInstance().getList());
        setEntries(sections);
    }
}
