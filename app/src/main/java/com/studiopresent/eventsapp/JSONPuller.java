package com.studiopresent.eventsapp;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * JSON handler class which connects, fetch and parse the json data
 */


public class JSONPuller {

    private String jTitle;
    private String jStartDate;
    private String jImgSrc;
    private String urlString = null;
    private Drawable img;
    private JSONArray nodes;
    private int i;

    public volatile boolean parsingComplete = true;


    // The URL of the json code. i = the index of the event in the json array.
    public JSONPuller(int i) {
        this.urlString = "http://development.studiopresent.info/eventsapp/get-data/json";
        this.i = i;
    }

    public String getjTitle() {
        return jTitle;
    }

    public String getjStartDate() {
        return jStartDate;
    }

    public String getjImgSrc() {
        return jImgSrc;
    }

    public Drawable getImg() {
        return img;
    }

    public JSONArray getNodes() {return nodes; }


    @SuppressLint("NewApi")
    public void readAndParseJSON(String in) {
        try {
            JSONObject reader = new JSONObject(in);
            nodes = reader.getJSONArray("nodes");
            JSONObject jRealObj = nodes.getJSONObject(i);
            JSONObject j2 = jRealObj.getJSONObject("node");
            jTitle = j2.getString("title");
            jStartDate = j2.getString("Start Date");
            JSONObject imgobj = j2.getJSONObject("Image hdpi");
            jImgSrc = imgobj.getString("src");
            Bitmap myImage = getBitmapFromURL(jImgSrc);
            img = new BitmapDrawable(myImage);
            parsingComplete = false;


        } catch (Exception e) {
            // TODO Auto-generated catch block
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