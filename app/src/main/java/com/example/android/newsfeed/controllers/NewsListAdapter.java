package com.example.android.newsfeed.controllers;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.example.android.newsfeed.models.NewsModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the list
 *
 * @package com.example.android.newsfeed
 * (c) 2018, Igor Korovchenko.
 */
public class NewsListAdapter extends RecyclerView.Adapter<NewsViewHolder> {

    private List<NewsModel> items = new ArrayList<>();

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return NewsViewHolder.inflate(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<NewsModel> items) {
        this.items = items;
    }
}
