package com.example.android.newsfeed.loaders;

import android.content.Context;
import android.content.Loader;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.newsfeed.BuildConfig;
import com.example.android.newsfeed.R;
import com.example.android.newsfeed.models.SectionModel;

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
 * Class
 *
 * @package com.example.android.newsfeed.loaders
 * (c) 2018, Igor Korovchenko.
 */
public class SectionsLoader extends Loader<List<SectionModel>> {

    public static final Integer LOADER_ID = 2;
    private GetSectionsTask getSectionsTask;
    private OnSectionsLoader sectionsLoader;
    
    public SectionsLoader(Context context, OnSectionsLoader sectionsLoader) {
        super(context);
        this.sectionsLoader = sectionsLoader;
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        if (getSectionsTask != null) {
            getSectionsTask.cancel(true);
        }
        getSectionsTask = new GetSectionsTask(sectionsLoader);
        getSectionsTask.execute(
                getContext().getString(R.string.sections_api_url),
                BuildConfig.THE_GUARDIAN_API_KEY,
                getContext().getResources().getString(R.string.json_response_key),
                getContext().getResources().getString(R.string.json_results_list_key),
                getContext().getResources().getString(R.string.json_title_key),
                getContext().getResources().getString(R.string.json_id_key)
        );
    }

    public interface OnSectionsLoader {
        void onSectionsLoadingFinished(List<SectionModel> newsList);
    }

    private static class GetSectionsTask extends AsyncTask<String, Void, List<SectionModel>> {

        private static final String TAG = "GetSectionsTask";
        private static final Integer READ_TIMEOUT = 10000;
        private static final Integer CONNECT_TIMEOUT = 15000;
        private SectionsLoader.OnSectionsLoader sectionsLoader;

        GetSectionsTask(SectionsLoader.OnSectionsLoader sectionsLoader) {
            this.sectionsLoader = sectionsLoader;
        }

        @Override
        protected List<SectionModel> doInBackground(String... strings) {
            Log.d(TAG, String.valueOf(strings.length));
            List<SectionModel> jsonResponse = new ArrayList<>();
            StringBuilder urlString = new StringBuilder();
            urlString.append(strings[0])
                    .append("?api-key=").append(strings[1]);
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
                    jsonResponse = extractSectionsFromJson(
                            readFromStream(inputStream),
                            strings);
                }
            } catch (IOException e) {
                e.printStackTrace();
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
        protected void onPostExecute(List<SectionModel> result) {
            super.onPostExecute(result);
            sectionsLoader.onSectionsLoadingFinished(result);
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
        private List<SectionModel> extractSectionsFromJson(String jsonString, String... strings) {
            if (TextUtils.isEmpty(jsonString)) {
                return null;
            }
            String responseKey  = strings[2];
            String resultKey    = strings[3];
            String titleKey     = strings[4];
            String idKey        = strings[5];
            List<SectionModel> list = new ArrayList<>();
            try {
                JSONObject baseJsonResponse = new JSONObject(jsonString);
                JSONObject sectionsResponse = baseJsonResponse.getJSONObject(responseKey);
                JSONArray sectionsArray = sectionsResponse.getJSONArray(resultKey);
                if (sectionsArray.length() > 0) {
                    for (Integer i = 0; i < sectionsArray.length(); i++) {
                        JSONObject sectionInfo = sectionsArray.getJSONObject(i);
                        String sectionTitle = sectionInfo.getString(titleKey);
                        String sectionId = sectionInfo.getString(idKey);
                        list.add(new SectionModel(sectionId, sectionTitle));
                    }
                }
                return list;
            } catch (JSONException e) {
                e.printStackTrace();
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
