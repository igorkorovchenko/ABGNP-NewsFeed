package com.example.android.newsfeed.loaders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.newsfeed.BuildConfig;
import com.example.android.newsfeed.R;
import com.example.android.newsfeed.models.NewsModel;

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

    private GetNewsTask getNewsTask;
    private String searchString;
    private OnNewsLoader newsLoader;
    @SuppressLint("StaticFieldLeak")
    private Context context;

    public NewsLoader(Context context, Bundle args, OnNewsLoader newsLoader) {
        super(context);
        this.context = context;
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String section = preferences.getString("pref_section", "");
        if (getNewsTask != null) {
            getNewsTask.cancel(true);
        }
        getNewsTask = new GetNewsTask(newsLoader);
        getNewsTask.execute(
                getContext().getString(R.string.news_api_url),                  // 0
                BuildConfig.THE_GUARDIAN_API_KEY,                               // 1
                searchString,                                                   // 2
                getContext().getString(R.string.loader_error_response),         // 3
                getContext().getString(R.string.loader_error_retrieving_json),  // 4
                getContext().getString(R.string.json_response_key),             // 5
                getContext().getString(R.string.json_results_list_key),         // 6
                getContext().getString(R.string.json_title_key),                // 7
                getContext().getString(R.string.json_section_key),              // 8
                getContext().getString(R.string.json_url_key),                  // 9
                getContext().getString(R.string.json_date_key),                 // 10
                getContext().getString(R.string.json_tags_key),                 // 11
                getContext().getString(R.string.json_author_key),               // 12
                getContext().getString(R.string.json_exception_message),        // 13
                section                                                         // 14
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
            Log.d(TAG, String.valueOf(strings.length));
            List<NewsModel> jsonResponse = new ArrayList<>();
            StringBuilder urlString = new StringBuilder();
            urlString.append(strings[0])
                    .append("?api-key=").append(strings[1])
                    .append(strings[2])
                    .append("&show-tags=contributor")
                    .append((!strings[14].equals("")) ? "&section=" + strings[14] : "");
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
                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = extractNewsFromJson(
                            readFromStream(inputStream),
                            strings);
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
        private List<NewsModel> extractNewsFromJson(String newsJson, String... strings) {
            if (TextUtils.isEmpty(newsJson)) {
                return null;
            }

            String responseKey      = strings[5];
            String resultKey        = strings[6];
            String titleKey         = strings[7];
            String sectionKey       = strings[8];
            String urlKey           = strings[9];
            String dateKey          = strings[10];
            String tagsKey          = strings[11];
            String authorKey        = strings[12];
            String exceptionMessage = strings[13];

            try {
                JSONObject baseJsonResponse = new JSONObject(newsJson);
                JSONObject newsResponse = baseJsonResponse.getJSONObject(responseKey);
                JSONArray newsArray = newsResponse.getJSONArray(resultKey);
                List<NewsModel> newList = new ArrayList<>();
                if (newsArray.length() > 0) {
                    for (Integer i = 0; i < newsArray.length(); i++) {
                        JSONObject newsInfo = newsArray.getJSONObject(i);
                        String newsTitle = newsInfo.getString(titleKey);
                        String newsSection = newsInfo.getString(sectionKey);
                        String newsUrl = newsInfo.getString(urlKey);
                        String newsDate = newsInfo.getString(dateKey);
                        StringBuilder newsAuthor = new StringBuilder();
                        JSONArray newsTagsArray = newsInfo.getJSONArray(tagsKey);
                        for (Integer j = 0; j < newsTagsArray.length(); j++) {
                            JSONObject newsTag = newsTagsArray.getJSONObject(j);
                            newsAuthor.append(newsTag.getString(authorKey)).append(", ");
                        }
                        if (newsAuthor.length() > 0) {
                            newsAuthor.delete(newsAuthor.lastIndexOf(", "), newsAuthor.length());
                        }
                        newList.add(new NewsModel(
                                newsTitle,
                                newsSection,
                                newsUrl,
                                newsDate,
                                String.valueOf(newsAuthor)
                        ));
                    }
                }
                return newList;
            } catch (JSONException e) {
                Log.e(TAG, exceptionMessage, e);
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
