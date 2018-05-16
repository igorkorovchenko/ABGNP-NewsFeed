package com.example.android.newsfeed;

import android.content.Context;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Loader for the news list
 *
 * @package com.example.android.newsfeed
 * (c) 2018, Igor Korovchenko.
 */
public class NewsLoader extends Loader<List<NewsModel>> {

    public static final Integer LOADER_ID = 1;
    public static final String ARGS_KEY_SEARCH = "search";

    private GetNewsTask getTimeTask;
    private String searchString;
    private OnNewsLoader newsLoader;

    NewsLoader(Context context, Bundle args, OnNewsLoader newsLoader) {
        super(context);
        if (args != null) {
            searchString = args.getString(ARGS_KEY_SEARCH);
        }
        if (TextUtils.isEmpty(searchString)) {
            searchString = "";
        } else {
            searchString = "&q=" + searchString;
        }
        this.newsLoader = newsLoader;
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        if (getTimeTask != null) {
            getTimeTask.cancel(true);
        }

        getTimeTask = new GetNewsTask(newsLoader);
        getTimeTask.execute(
                getContext().getString(R.string.news_api_url),
                getContext().getString(R.string.news_api_key),
                searchString,
                getContext().getString(R.string.loader_error_response),
                getContext().getString(R.string.loader_error_retrieving_json),
                getContext().getString(R.string.json_response_key),
                getContext().getString(R.string.json_results_list_key),
                getContext().getString(R.string.json_title_key),
                getContext().getString(R.string.json_section_key),
                getContext().getString(R.string.json_url_key),
                getContext().getString(R.string.json_date_key),
                getContext().getString(R.string.json_exception_message)
        );
    }

    public interface OnNewsLoader {
        void onNewsLoadingFinished(List<NewsModel> newsList);
    }

    private static class GetNewsTask extends AsyncTask<String, Void, List<NewsModel>> {

        private static final String TAG = "GetNewsTask";
        private static final Integer READ_TIMEOUT = 10000;
        private static final Integer CONNECT_TIMEOUT = 15000;

        private OnNewsLoader newsLoader;

        GetNewsTask(OnNewsLoader newsLoader) {
            this.newsLoader = newsLoader;
        }

        @Override
        protected List<NewsModel> doInBackground(String... strings) {
            List<NewsModel> jsonResponse = new ArrayList<>();
            StringBuilder urlString = new StringBuilder();
            urlString.append(strings[0]).append("?api-key=").append(strings[1]).append(strings[2]);
            Log.d(TAG, String.valueOf(urlString));
            URL url = createUrl(String.valueOf(urlString));
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(READ_TIMEOUT);
                urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = extractNewsFromJson(
                            readFromStream(inputStream),
                            strings[3],
                            strings[4],
                            strings[5],
                            strings[6],
                            strings[7],
                            strings[8],
                            strings[9],
                            strings[10]);
                } else {
                    Log.e(TAG, strings[3] + urlConnection.getResponseCode());
                }
            } catch (IOException e) {
                Log.e(TAG, strings[4], e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return jsonResponse;
        }

        @Override
        protected void onPostExecute(List<NewsModel> result) {
            super.onPostExecute(result);
            newsLoader.onNewsLoadingFinished(result);
        }

        @NonNull
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        @Nullable
        private List<NewsModel> extractNewsFromJson(String newsJson, String... keys) {
            if (TextUtils.isEmpty(newsJson)) {
                return null;
            }

            try {
                JSONObject baseJsonResponse = new JSONObject(newsJson);
                JSONObject newsResponse = baseJsonResponse.getJSONObject(keys[2]);
                JSONArray newsArray = newsResponse.getJSONArray(keys[3]);

                if (newsArray.length() > 0) {
                    List<NewsModel> newList = new ArrayList<>();
                    for (Integer i = 0; i < newsArray.length(); i++) {
                        JSONObject newsInfo = newsArray.getJSONObject(i);
                        String newsTitle = newsInfo.getString(keys[4]);
                        String newsSection = newsInfo.getString(keys[5]);
                        String newsUrl = newsInfo.getString(keys[6]);
                        String newsDate = newsInfo.getString(keys[7]);
                        newList.add(new NewsModel(newsTitle, newsSection, newsUrl, newsDate));
                    }
                    return newList;
                }
            } catch (JSONException e) {
                Log.e(TAG, keys[6], e);
            }
            return null;
        }

        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return url;
        }
    }

}
