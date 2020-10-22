package com.example.android.quakereport.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.example.android.quakereport.model.Earthquake;

import org.json.JSONArray;
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
import java.util.Date;

public class EarthquakeLoader extends AsyncTaskLoader <ArrayList <Earthquake> > {

    private String stringURL;

    public EarthquakeLoader(Context context, String stringURL) {
        super(context);

        this.stringURL = stringURL;
    }

    @Override
    public ArrayList<Earthquake> loadInBackground() {

        URL url = getURL(stringURL);

        ArrayList<Earthquake> arrayList = null;

        try {
            String jsonString = makeRequest(url);
            arrayList = getEarthquakeData(jsonString);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return arrayList;
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

        try{
            Thread.sleep(1500);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }

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
}
