package com.example.android.newsfeed.models;

/**
 * Class
 *
 * @package com.example.android.newsfeed.models
 * (c) 2018, Igor Korovchenko.
 */
public class SectionModel {
    private String id;
    private String title;

    public SectionModel(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
