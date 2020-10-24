/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.quakereport.adapter.CustomAdapter;
import com.example.android.quakereport.loader.EarthquakeLoader;
import com.example.android.quakereport.model.Earthquake;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EarthquakeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks< ArrayList<Earthquake> >{

    private CustomAdapter adapter;
    private ListView earthquakeListView;
    private TextView emptyView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private ConnectivityManager connectivityManager;
    private String requestURL = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&eventtype=earthquake&limit=100&orderby=time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        earthquakeListView = (ListView) findViewById(R.id.list);
        emptyView = (TextView)findViewById(R.id.emptyView);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.pullToRefresh);
        progressBar = (ProgressBar)findViewById(R.id.progressbar);

        earthquakeListView.setEmptyView(emptyView);

        adapter = new CustomAdapter(this, new ArrayList<Earthquake>());
        earthquakeListView.setAdapter(adapter);

        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Earthquake object = adapter.getItem(position);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(object.getUrl()));
                startActivity(intent);
            }
        });

        connectivityManager = (ConnectivityManager)getSystemService(EarthquakeActivity.CONNECTIVITY_SERVICE);

        if(connectivityManager.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING ||
                connectivityManager.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTING ||
                connectivityManager.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED ) {

            getLoaderManager().initLoader(0,null,EarthquakeActivity.this).forceLoad();

        }

        else {
            adapter.clear();
            progressBar.setVisibility(View.GONE);
            emptyView.setText("No Internet :(");
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(connectivityManager.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING ||
                        connectivityManager.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTING ||
                        connectivityManager.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED ) {

                    getLoaderManager().initLoader(0,null,EarthquakeActivity.this).forceLoad();
                    swipeRefreshLayout.setRefreshing(false);
                }

                else {
                    adapter.clear();
                    progressBar.setVisibility(View.GONE);
                    emptyView.setText("No Internet :(");
                    swipeRefreshLayout.setRefreshing(false);

                }
            }
        });

    }

    @Override
    public Loader<ArrayList<Earthquake>> onCreateLoader(int i, Bundle bundle) {
        return new EarthquakeLoader(this, requestURL);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Earthquake>> loader, ArrayList<Earthquake> arrayList) {
        adapter.clear();
        progressBar.setVisibility(View.GONE);

        if(arrayList == null || arrayList.isEmpty()){
            emptyView.setText("Error in loading data :(");
            return;
        }

        adapter.addAll(arrayList);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Earthquake>> loader) {
        adapter.clear();
    }

    /*

    private class EarthquakeSync extends AsyncTask<String, Void, ArrayList<Earthquake>> {

        @Override
        protected ArrayList<Earthquake> doInBackground(String... strings) {

            String urlString  = strings[0];
            URL url = getURL(urlString);

            ArrayList<Earthquake> arrayList = null;

            try {
                String jsonString = makeRequest(url);
                arrayList = getEarthquakeData(jsonString);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return arrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<Earthquake> arrayList) {
            if(arrayList == null)
                return;

            adapter.clear();
            adapter.addAll(arrayList);
        }
    }

    private URL getURL(String string) {
        URL url = null;
        try {
            url = new URL(string);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    private String makeRequest(URL url) throws IOException {

        String jsonString = "";
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;

        if(url == null)
            return jsonString;

        try {
            httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setConnectTimeout(15000);

            httpURLConnection.connect();

            if(httpURLConnection.getResponseCode() == 200) {
                inputStream = httpURLConnection.getInputStream();
                jsonString = readFromStream(inputStream);
            }

            else
            {
                Log.v("Connection Error", "Unabble to connect to the server");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(httpURLConnection!=null)
            httpURLConnection.disconnect();

        if(inputStream!=null)
            inputStream.close();

        return jsonString;
    }

    private String readFromStream(InputStream inputStream) throws IOException {

        StringBuilder stringBuilder = new StringBuilder();
        InputStreamReader inputStreamReader = null;

        if(inputStream != null ){
            inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line = bufferedReader.readLine();

            while(line!=null)
            {
                stringBuilder.append(line);
                line = bufferedReader.readLine();
            }
        }

        return stringBuilder.toString();
    }

    private ArrayList<Earthquake> getEarthquakeData(String JSONresponse) {

        ArrayList<Earthquake> arrayList = new ArrayList<Earthquake>();

        if(JSONresponse == null || JSONresponse.isEmpty())
            return null;

        try {
            JSONObject jsonObject = new JSONObject(JSONresponse);
            JSONArray jsonArray = jsonObject.getJSONArray("features");

            for(int i=0;i<jsonArray.length();i++)
            {
                JSONObject temp = jsonArray.getJSONObject(i).getJSONObject("properties");

                String location = temp.optString("place");
                double magnitude = temp.optDouble("mag");
                Date date = new Date(temp.optLong("time"));
                String url = temp.optString("url");

                arrayList.add(new Earthquake(location, date.toString(), magnitude,url));
            }

        }catch (Exception e ) {
            e.printStackTrace();
        }

        return  arrayList;
    }
    */
}
