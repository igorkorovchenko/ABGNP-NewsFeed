package com.example.android.newsfeed.models;

/**
 * Model of the news
 *
 * @package com.example.android.newsfeed
 * (c) 2018, Igor Korovchenko.
 */
public class NewsModel {
    private String sectionName;
    private String webTitle;
    private String webUrl;
    private String webPublicationDate;
    private String contributor;

    public NewsModel(String webTitle, String sectionName, String webUrl, String webPublicationDate, String contributor) {
        this.sectionName = sectionName;
        this.webTitle = webTitle;
        this.webUrl = webUrl;
        this.webPublicationDate = webPublicationDate;
        this.contributor = contributor;
    }

    public String getSectionName() {
        return sectionName;
    }

    public String getWebTitle() {
        return webTitle;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public String getWebPublicationDate() {
        return webPublicationDate;
    }

    public String getContributor() {
        return contributor;
    }
}
