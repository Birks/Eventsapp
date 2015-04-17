package com.studiopresent.eventsapp;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;
import com.studiopresent.eventsapp.gson.EventsJson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
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
    private Context context;
    private FileSaveMethods fileIOManager;

    public volatile boolean parsingComplete = true;


    // The URL of the json code. i = the index of the event in the json array.
    public JSONPuller(MainActivity ma, Context context) {
        this.urlString = "http://development.studiopresent.info/eventsapp/get-data/json";
        this.ma = ma;
        this.context = context;
        this.fileIOManager = new FileSaveMethods(ma);
        events = new ArrayList<EventInfo>();
    }

    public List<EventInfo> getEvents() {
        return events;
    }

    @SuppressLint("NewApi")
    public void readAndParseJSON(String in) {
        try {

            // GSON initalize
            Gson gson = new GsonBuilder().create();
            EventsJson gObj = gson.fromJson(in, EventsJson.class);
//            Log.v("GSON", gObj.toString());

//            JSONObject reader = new JSONObject(in);
//            JSONArray nodes = reader.getJSONArray("nodes");

            // Array is needed to store multiple events
            for (int i = 0; i < gObj.nodes.length; i++) {
//                JSONObject jRealObj = nodes.getJSONObject(i);
//                JSONObject j2 = jRealObj.getJSONObject("node");

                // JSON Data main part
                EventInfo ei = new EventInfo();
                ei.title = gObj.nodes[i].node.title;

                ei.dStartDate=CalendarMaker.generateFromString(gObj.nodes[i].node.startDate);
                ei.startDate=ei.dStartDate.getSerbianDateFormat();

                ei.endDate = gObj.nodes[i].node.endDate;
                ei.body = gObj.nodes[i].node.body;
                ei.name = gObj.nodes[i].node.name;

                ei.city = gObj.nodes[i].node.city;
                ei.street = gObj.nodes[i].node.street;
                ei.latitude = gObj.nodes[i].node.latitude;
                ei.longitude = gObj.nodes[i].node.longitude;

                ei.id = i;

                ei.imageSrc = gObj.nodes[i].node.imageHdpi.src;

                // Online vs offline mode
                if (isNetworkAvailable(context)) {
                    // When network available then download from server and save into file
                    ei.imageBitmap = Picasso.with(context).load( gObj.nodes[i].node.imageHdpi.src).get();


                    File file = new File(context.getFilesDir().getAbsolutePath() + "/pic_" + ei.id + ".jpg");
                    try {
                        file.createNewFile();
                        FileOutputStream ostream = new FileOutputStream(file);
                        ei.imageBitmap.compress(Bitmap.CompressFormat.JPEG, 75, ostream);
                        ostream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    // When not connected to network then loads from file
                    Log.v("global_index", String.valueOf(Uri.fromFile(new File(context.getFilesDir().getAbsolutePath() + "/pic_" + ei.id + ".jpg"))));
                    ei.imageSrc = String.valueOf(Uri.fromFile(new File(context.getFilesDir().getAbsolutePath() + "/pic_" + ei.id + ".jpg")));
                    ei.imageBitmap = Picasso.with(context).load(ei.imageSrc).get();
                }


                // OnlClickListener added for the dynamic rlayout onClick function
                // An ID is given to every card, and at onlcick the DetailsActivity is opened with a given ID
                ei.onClickListener = new View.OnClickListener() {
                    public void onClick(View v) {
                        Log.v("openEvent", String.valueOf(v.getId()));
                        ma.openEvent(v.getId());
                    }
                };

                // Create new Alarm when downloading JSON
                new AlarmReceiver().setAlarm(context,ei);

                // Add the object to the event array
                events.add(ei);

            }

            // Saving json string to file, for offline use
            fileIOManager.saveToFile("json_string", in);
            Log.v("fileIOManager", "JSON saved to file");
            Log.v("fileIOManager", fileIOManager.readFromFile("json_string"));
            parsingComplete = false;


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This part connects and downloads the JSON data
    public void fetchJSON() {
        if (isNetworkAvailable(context)) {
            // available network
            Log.v("FetchJSON", "Network available");

            parsingComplete = true;
            Thread thread = new Thread(new Runnable() {
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
                        Log.v("FetchJson", "Connection error");
                        e.printStackTrace();
                    }
                }
            });

            thread.start();

        } else {
            // no network
            Log.v("FetchJSON", "No network available");
            readAndParseJSON(fileIOManager.readFromFile("json_string"));
        }
    }

    static String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    // Checks whether the connection is available to the internet
    public static boolean isNetworkAvailable(Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }

}
