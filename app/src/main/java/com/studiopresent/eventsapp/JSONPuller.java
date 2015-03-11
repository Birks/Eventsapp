package com.studiopresent.eventsapp;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/*
 * JSON handler class which connects, fetch and parse the json data
 */


public class JSONPuller {

    private String urlString = null;
    private List<EventInfo> events;
    private MainActivity ma;

    public volatile boolean parsingComplete = true;


    // The URL of the json code. i = the index of the event in the json array.
    public JSONPuller(MainActivity ma) {
        this.urlString = "http://development.studiopresent.info/eventsapp/get-data/json";
        this.ma=ma;
        events = new ArrayList<EventInfo>();
    }

    public List<EventInfo> getEvents() {
       return events;
    }

    @SuppressLint("NewApi")
    public void readAndParseJSON(String in) {
        try {
            JSONObject reader = new JSONObject(in);
            JSONArray nodes = reader.getJSONArray("nodes");

            // Array is needed to store multiple events
            for (int i = 0; i < nodes.length(); i++) {
                JSONObject jRealObj = nodes.getJSONObject(i);
                JSONObject j2 = jRealObj.getJSONObject("node");

                // JSON Data main part
                EventInfo ei = new EventInfo();
                ei.title = j2.getString("title");
                Log.v("title",j2.getString("title"));
                ei.startDate = j2.getString("startDate");
                ei.endDate = j2.getString("endDate");
                ei.body = j2.getString("body");


                JSONObject imgobj = j2.getJSONObject("imageHdpi");
                Bitmap myImage = getBitmapFromURL(imgobj.getString("src"));
                ei.imageHdpi = new BitmapDrawable(myImage);

                // OnlClickListener added for the dynamic rlayout onClick function
                // An ID is given to every card, and at onlcick the DetailsActivity is opened with a given ID
                ei.onClickListener = new View.OnClickListener() {
                public void onClick(View v) {
                    Log.v("openEvent", String.valueOf(v.getId()));
                    ma.openEvent(v.getId());
                }};

                // Add the object to the event array
                events.add(ei);

            }
            parsingComplete = false;


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Needed for the background image
    public Bitmap getBitmapFromURL(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }


    // This part connects and downloads the JSON data
    public void fetchJSON(){
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(100000);
                    conn.setConnectTimeout(150000);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    // Starts the query
                    conn.connect();
                    InputStream stream = conn.getInputStream();
                    String data = convertStreamToString(stream);
                    readAndParseJSON(data);
                    stream.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
    static String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}