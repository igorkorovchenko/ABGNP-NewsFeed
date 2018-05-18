package com.example.android.newsfeed.controllers;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.newsfeed.R;
import com.example.android.newsfeed.models.NewsModel;

/**
 * ViewHolder for each article
 *
 * @package com.example.android.newsfeed
 * (c) 2018, Igor Korovchenko.
 */
public class NewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView newsTitle;
    private TextView newsSection;
    private TextView newsDate;
    private TextView newsAuthor;
    private String newsURL;

    public static NewsViewHolder inflate(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item, parent, false);
        return new NewsViewHolder(v);
    }

    private NewsViewHolder(View itemView) {
        super(itemView);
        newsTitle = itemView.findViewById(R.id.news_title);
        newsSection = itemView.findViewById(R.id.news_section);
        newsDate = itemView.findViewById(R.id.news_date);
        newsAuthor = itemView.findViewById(R.id.news_author);
        newsURL = "";
    }

    public void bind(NewsModel newsModel) {
        newsTitle.setText(newsModel.getWebTitle());
        newsTitle.setOnClickListener(this);
        newsSection.setText(newsModel.getSectionName());
        newsSection.setOnClickListener(this);
        newsDate.setText(newsModel.getWebPublicationDate());
        newsDate.setOnClickListener(this);
        newsAuthor.setText(newsModel.getContributor());
        newsURL = newsModel.getWebUrl();
    }

    @Override
    public void onClick(View v) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(newsURL));
            (v.getContext()).startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
}
