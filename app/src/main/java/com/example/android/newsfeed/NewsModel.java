package com.example.android.newsfeed;

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

    NewsModel(String webTitle, String sectionName, String webUrl, String webPublicationDate) {
        this.sectionName = sectionName;
        this.webTitle = webTitle;
        this.webUrl = webUrl;
        this.webPublicationDate = webPublicationDate;
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
}
