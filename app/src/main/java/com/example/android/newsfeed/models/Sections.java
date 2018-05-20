package com.example.android.newsfeed.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Class
 *
 * @package com.example.android.newsfeed.models
 * (c) 2018, Igor Korovchenko.
 */
public class Sections {

    private static Sections instance;
    private List<SectionModel> list;
    private Boolean empty = true;

    public static Sections getInstance(){
        if (instance == null) {
            instance = new Sections();
            instance.list = new ArrayList<>();
            instance.list.add(new SectionModel("", "No selected"));
        }
        return instance;
    }

    private Sections() {}

    public void addItems(List<SectionModel> sectionsList) {
        list.addAll(sectionsList);
        empty = false;
    }

    public List<SectionModel> getList() {
        return list;
    }

    public Boolean isEmpty() {
        return empty;
    }
}
