package com.k09.opendatachallenge;

import android.media.UnsupportedSchemeException;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.k09.opendatachallenge.data.OdptData;
import com.k09.opendatachallenge.data.Railway;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class OdptAccessor {
    private static final String LOG_TAG = OdptAccessor.class.getSimpleName();

    public static final String DATA_TYPE_RAILWAY = "Railway";
    public static final String DATA_TYPE_STATION = "Station";

    public static final int ERROR_TYPE_INTERNAL = 0;
    public static final int ERROR_TYPE_ODPT_SERVICE_UNAVAILABLE = 1;

    public interface Callback {
        void onGet(String dataType, OdptData[] data);

        void onError(int errorType);
    }

    private static class ConnectionAsyncTask extends AsyncTask<String, Integer, OdptData[]> {
        private static final String END_POINT_URL_HEADER = "https://api-tokyochallenge.odpt.org/api/v4/odpt:";
        private static final String CONSUMER_KEY = BuildConfig.API_KEY;

        private WeakReference<OdptAccessor> mOdptAccesorWeakReference;
        private String mType;

        private ConnectionAsyncTask(OdptAccessor accessor) {
            mOdptAccesorWeakReference = new WeakReference<>(accessor);
        }

        @Override
        protected OdptData[] doInBackground(String... strings) {
            mType = strings[0];
            OdptData[] ret = null;
            try {
                String json;
                switch (mType) {
                    case DATA_TYPE_RAILWAY:
                        String railwayName = strings[1];
                        json = getRailwayJSON(railwayName);
                        ret = parseRailwayJson(json);
                        break;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                errorCallback(ERROR_TYPE_INTERNAL);
            }
            return ret;
        }

        @Override
        protected void onPostExecute(OdptData[] odptData) {
            super.onPostExecute(odptData);
            OdptAccessor accessor = mOdptAccesorWeakReference.get();
            if (accessor != null) {
                accessor.mCallback.onGet(mType, odptData);
            }
        }

        private void errorCallback(int errorType) {
            OdptAccessor accessor = mOdptAccesorWeakReference.get();
            if (accessor != null) {
                accessor.mCallback.onError(errorType);
            }
        }

        private String getRailwayJSON(String railwayName) throws IOException {
                final String URL_STRING =
                        END_POINT_URL_HEADER
                                + DATA_TYPE_RAILWAY
                                + "?acl:consumerKey=" + CONSUMER_KEY
                                + "&dc:title=" + URLEncoder.encode(railwayName, "UTF-8");
            return getJSON(URL_STRING);
        }

        private Railway[] parseRailwayJson(String jsonString) throws JSONException {
            Log.d(LOG_TAG, "jsonString:" + jsonString);
            JSONArray jsonArray = new JSONArray(jsonString);
            Railway[] ret = new Railway[jsonArray.length()];

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Railway railway = new Railway();
                railway.date = jsonObject.getString("dc:date");
                railway.sameAs = jsonObject.getString("owl:sameAs");
                railway.operator = jsonObject.getString("odpt:operator");
                railway.title = jsonObject.getString("dc:title");
                JSONObject railwayTitleJSONObject = jsonObject.getJSONObject("odpt:railwayTitle");
                railway.railwayTitle.ja = railwayTitleJSONObject.getString("ja");
                railway.railwayTitle.en = railwayTitleJSONObject.getString("en");
                JSONArray stationOrderJSONArray = jsonObject.getJSONArray("odpt:stationOrder");
                railway.stationOrder = new Railway.StationOrder[stationOrderJSONArray.length()];
                for (int j = 0; j < stationOrderJSONArray.length(); j++) {
                    JSONObject stationOrderJSONObject = stationOrderJSONArray.getJSONObject(j);
                    Railway.StationOrder stationOrder = new Railway.StationOrder();
                    stationOrder.index = stationOrderJSONObject.getInt("odpt:index");
                    stationOrder.station = stationOrderJSONObject.getString("odpt:station");
                    JSONObject stationTitleJSONObject = stationOrderJSONObject.getJSONObject("odpt:stationTitle");
                    stationOrder.stationTitle.ja = stationTitleJSONObject.getString("ja");
                    stationOrder.stationTitle.en = stationTitleJSONObject.getString("en");
                    railway.stationOrder[j] = stationOrder;
                }
                ret[i] = railway;
            }

            return ret;
        }

        private String getJSON(String urlString) throws IOException {
            Log.d(LOG_TAG, "request URL:" + urlString);
            // コードは以下のページを参考にしました
            // https://web.plus-idea.net/2016/08/httpurlconnection-post-get-proxy-sample/
            StringBuilder result = new StringBuilder();
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int status = connection.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                // 通信に成功した
                // テキストを取得する
                final InputStream in = connection.getInputStream();
                String encoding = connection.getContentEncoding();
                if (null == encoding) {
                    encoding = "UTF-8";
                }
                final InputStreamReader inReader = new InputStreamReader(in, encoding);
                final BufferedReader bufReader = new BufferedReader(inReader);
                String line = null;
                // 1行ずつテキストを読み込む
                while ((line = bufReader.readLine()) != null) {
                    result.append(line);
                }
                bufReader.close();
                inReader.close();
                in.close();
            } else {
                Log.e(LOG_TAG, "get json failed. status code=" + status);
            }
            return result.toString();
        }
    }

    private Callback mCallback;

    public OdptAccessor(@NonNull Callback callback) {
        setCallback(callback);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void getRailway(String railwayTitle) {
        ConnectionAsyncTask task = new ConnectionAsyncTask(this);
        task.execute(DATA_TYPE_RAILWAY, railwayTitle);
    }
}
