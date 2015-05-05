package com.studiopresent.eventsapp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;
import com.studiopresent.eventsapp.gson.EventsJson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/*
 * JSON handler class which connects, fetch and parse the json data
 */


public class JSONPuller {

    private String urlString = null;
    public List<EventInfo> events;
    private MainActivity ma;
    private Context context;
    private FileSaveMethods fileIOManager;
    private List<EventInfo> oldEvents;
    public static boolean hasInternetConnection = true;

    public volatile boolean parsingComplete = true;


    // The URL of the json code. i = the index of the event in the json array.
    public JSONPuller(MainActivity ma, Context context) {
        this.urlString = "http://development.studiopresent.info/eventsapp/get-data/json";
        this.ma = ma;
        this.context = context;
        this.fileIOManager = new FileSaveMethods(ma);
        events = new ArrayList<>();
    }

    public List<EventInfo> getEvents() {
        return events;
    }

    @SuppressLint("NewApi")
    public void readAndParseJSON(String in) {
        try {
            Log.v("GSON", "Parsing started...");

            // GSON initalize
            Gson gson = new GsonBuilder().create();
            EventsJson gObj = gson.fromJson(in, EventsJson.class);
//            Log.v("GSON", gObj.toString());

            events.clear();
            // Array is needed to store multiple events
            for (int i = 0; i < gObj.nodes.length; i++) {

                // JSON Data main part
                EventInfo ei = new EventInfo();
                ei.title = gObj.nodes[i].node.title;

                ei.dStartDate = CalendarMaker.generateFromString(gObj.nodes[i].node.startDate);
                ei.startDate = ei.dStartDate.getSerbianDateFormat();

                ei.updatedDate = gObj.nodes[i].node.updatedDate;

                ei.endDate = gObj.nodes[i].node.endDate;
                ei.body = gObj.nodes[i].node.body;
                ei.name = gObj.nodes[i].node.name;

                ei.city = gObj.nodes[i].node.city;
                ei.street = gObj.nodes[i].node.street;
                ei.latitude = gObj.nodes[i].node.latitude;
                ei.longitude = gObj.nodes[i].node.longitude;

                ei.id = i;
                ei.nid = Integer.parseInt(gObj.nodes[i].node.nid);

                ei.imageSrc = gObj.nodes[i].node.imageHdpi.src;

                // Static map url string

                String coord = ei.latitude + "%2C" + ei.longitude;
                String mapStr = "http://maps.google.com/maps/api/staticmap?center=" + coord + "&zoom=17&size=480x240&sensor=false&format=jpg&markers=" + coord;


                // Online vs offline mode
                if (hasInternetConnection) {
                    ei.mapURLSrc = mapStr;
                    Log.v("mapURL", ei.mapURLSrc);
                    // When network available then download from server and save into file
                    ei.imageBitmap = Picasso.with(context).load(gObj.nodes[i].node.imageHdpi.src).get();

                    ei.mapBitmap = Picasso.with(context).load(mapStr).get();

                    // Static Image save
                    File fileStaticMap = new File(context.getFilesDir().getAbsolutePath() + "/picmap_" + ei.id + ".jpg");
                    try {
                        fileStaticMap.createNewFile();
                        FileOutputStream ostream = new FileOutputStream(fileStaticMap);
                        ei.mapBitmap.compress(Bitmap.CompressFormat.JPEG, 75, ostream);
                        ostream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Main Image save
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
                    Log.v("global_index", String.valueOf(Uri.fromFile(new File(context.getFilesDir().getAbsolutePath() + "/picmap_" + ei.id + ".jpg"))));

                    ei.imageSrc = String.valueOf(Uri.fromFile(new File(context.getFilesDir().getAbsolutePath() + "/pic_" + ei.id + ".jpg")));
                    ei.mapURLSrc = String.valueOf(Uri.fromFile(new File(context.getFilesDir().getAbsolutePath() + "/picmap_" + ei.id + ".jpg")));
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
//                new AlarmReceiver().setAlarm(context,ei);


//                // Check date update
//                if (!(oldEvents == null)) {
//                    Log.v("Updatedate old name", oldEvents.get(i).name);
//                    Log.v("Updatedate new name", ei.name);
//
//                    if (!oldEvents.get(i).updatedDate.equals(ei.updatedDate)) {
//                        Log.v("Dateupdate", "Update found!");
//                        Log.v("Dateupdate", "clock: " + ei.startDate);
//                        sendNotification(String.valueOf(ei.id),ei);
//                    } else {
//                        Log.v("Dateupdate", "No update found!");
//                    }
//                }


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

            // Fetching old JSON from file
            if (fileIOManager.fileExists("json_string")) {
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        readAndParseJSON(fileIOManager.readFromFile("json_string"));
                    }
                });

                thread.start();

                while (parsingComplete) ;
                Log.v("Updatedate", "Old Parsing completed");
                oldEvents = new ArrayList<>(events);
                events.clear();
            }


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
            hasInternetConnection = false;
            readAndParseJSON(fileIOManager.readFromFile("json_string"));
        }
    }

    static String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public void sendNotification(String id, EventInfo ei) {
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        int id_code = Integer.parseInt(id);

        // Read data from file

        Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra("ID", id);
        intent.putExtra("BITMAP", ei.imageSrc);
        intent.putExtra("TITLE", ei.title);
        intent.putExtra("STARTDATE", ei.startDate);
        intent.putExtra("NAME", ei.name);
        intent.putExtra("BODY", ei.body);
        intent.putExtra("CITY", ei.city);
        intent.putExtra("STREET", ei.street);
        intent.putExtra("LATITUDE", ei.latitude);
        intent.putExtra("LONGITUDE", ei.longitude);
        intent.putExtra("GPS", ei.latitude + ", " + ei.longitude);
        intent.putExtra("MAPURL", ei.mapURLSrc);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, id_code,
                intent, PendingIntent.FLAG_ONE_SHOT);

        String notiText="Event updated, starts at " + ei.dStartDate.getClockTime();


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(ei.title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(notiText))
                        .setContentText(notiText);

        mBuilder.setAutoCancel(true);
        mBuilder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;


        // Set a sound effect to the notification
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(Integer.parseInt(id), mBuilder.build());
    }

    // Checks whether the connection is available to the internet
    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();

            if (netInfo != null && netInfo.isConnected()) {

                //Network is available but check if we can get access from the network.
                URL url = new URL("http://217.65.100.93"); // development.studiopresent.info
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(5000); // Timeout 2 seconds.
                try {
                    urlc.connect();
                } catch (Exception e) {
                    Log.v("FetchJSON", "Error, no internet available or server is down.");
                    e.printStackTrace();
                }

                if (urlc.getResponseCode() == 200)  //Successful response.
                {
                    Log.v("FetchJSON", "Has internet");
                    return true;
                } else {
                    Log.v("FetchJSON", "NO INTERNET");
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

}
