package com.studiopresent.eventsapp;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.studiopresent.eventsapp.gson.EventsJson;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This {@code IntentService} does the app's actual work.
 * {@code SampleAlarmReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class SchedulingService extends IntentService {

    private static boolean parsingCompleted = true;
    private EventInfo ei;
    private List<EventInfo> oldEvents;
    private List<EventInfo> events;

    public SchedulingService() {
        super("SchedulingService");
    }

    // An ID used to post the notification.
    public static final int NOTIFICATION_ID = 1;

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.v("Alarm", "IntentExtra id: " + intent.getStringExtra("id"));

        if (intent.getStringExtra("id") == null) {
            Log.v("Alarm", "id is null");
            checkUpdates();
        } else {
            sendNotification(intent.getStringExtra("id"));
        }
        // The URL from which to fetch content.


        Log.v("Alarm", "Notification received!");

        // Release the wake lock provided by the BroadcastReceiver.
        AlarmReceiver.completeWakefulIntent(intent);

    }

    private void checkUpdates() {
        readJSON(0);
    }


    private class CheckUpdateTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            for (EventInfo olditem : oldEvents) {
//                Log.v("UpdateCheck", "Olditem: " + String.valueOf(olditem.nid));

                for (EventInfo newitem : events) {
//                    Log.v("UpdateCheck", "Newitem: " + String.valueOf(newitem.nid));

                    if (olditem.nid == newitem.nid) {
//                        Log.v("UpdateCheck", "Same item found " + olditem.nid + " " + olditem.title);
                        if (!olditem.updatedDate.equals(newitem.updatedDate)) {
                            olditem.updatedDate = newitem.updatedDate;
                            Log.v("UpdateCheck", "Update found!");
                            Log.v("UpdateCheck", "clock: " + newitem.startDate);
                            sendNotification(String.valueOf(newitem.id), newitem, true);
                        } else {
                            Log.v("UpdateCheck", "No update found!");
                        }
                    }
                }
            }


            return null;
        }

    }

    // Post a notification indicating whether a doodle was found.
    private void sendNotification(String id) {

        int id_code = Integer.parseInt(id);

        // Read data from file
        readJSONForNotification(id_code);

    }

    public void finishNotification(int id_code) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);


        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("ID", id_code);
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

        PendingIntent contentIntent = PendingIntent.getActivity(this, id_code,
                intent, PendingIntent.FLAG_ONE_SHOT);

        String notiText = "Starts at " + ei.dStartDate.getClockTime();


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.events_white)
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
        mNotificationManager.notify(id_code, mBuilder.build());
    }

    public void readNewJSON(int id, String url) {
        List<EventInfo> events;
        events = new ArrayList<>();
        Gson gson = new GsonBuilder().create();
        EventsJson gObj = gson.fromJson((url), EventsJson.class);

        events.clear();

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

            ei.imageSrc = gObj.nodes[i].node.imageHdpi.src;
            ei.nid = Integer.parseInt(gObj.nodes[i].node.nid);


            ei.imageSrc = String.valueOf(Uri.fromFile(new File(getBaseContext().getFilesDir().getAbsolutePath() + "/pic_" + ei.id + ".jpg")));
            ei.mapURLSrc = String.valueOf(Uri.fromFile(new File(getBaseContext().getFilesDir().getAbsolutePath() + "/picmap_" + ei.id + ".jpg")));

            events.add(ei);
        }
        this.events = events;

        new FileSaveMethods(this).saveToFile("json_string", url);
        List<EventInfo> orderedEvents = CalendarMaker.orderEvents(events);

        this.ei = orderedEvents.get(id);
        parsingCompleted = false;
        new CheckUpdateTask().execute();
        new CheckDeleteTask().execute();
        new CheckNewTask().execute();
    }

    public void readJSONForNotification(int id) {
        FileSaveMethods fileSaveMethods = new FileSaveMethods(this);

        Log.v("Alarm", "Alarm id in readJSON: " + id);
        // GSON initalize
        List<EventInfo> events;
        events = new ArrayList<>();
        Gson gson = new GsonBuilder().create();
        EventsJson gObj = gson.fromJson(fileSaveMethods.readFromFile("json_string"), EventsJson.class);

        events.clear();

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

            ei.imageSrc = gObj.nodes[i].node.imageHdpi.src;
            ei.nid = Integer.parseInt(gObj.nodes[i].node.nid);


            ei.imageSrc = String.valueOf(Uri.fromFile(new File(getBaseContext().getFilesDir().getAbsolutePath() + "/pic_" + ei.id + ".jpg")));
            ei.mapURLSrc = String.valueOf(Uri.fromFile(new File(getBaseContext().getFilesDir().getAbsolutePath() + "/picmap_" + ei.id + ".jpg")));

            events.add(ei);
        }
        this.oldEvents = events;

        List<EventInfo> orderedEvents = CalendarMaker.orderEvents(events);

        this.ei = orderedEvents.get(id);
        parsingCompleted = false;

        finishNotification(id);

    }

    public void readJSON(int id) {
        FileSaveMethods fileSaveMethods = new FileSaveMethods(this);

        Log.v("Alarm", "Alarm id in readJSON: " + id);
        // GSON initalize
        List<EventInfo> events;
        events = new ArrayList<>();
        Gson gson = new GsonBuilder().create();
        EventsJson gObj = gson.fromJson(fileSaveMethods.readFromFile("json_string"), EventsJson.class);

        events.clear();

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

            ei.imageSrc = gObj.nodes[i].node.imageHdpi.src;
            ei.nid = Integer.parseInt(gObj.nodes[i].node.nid);


            ei.imageSrc = String.valueOf(Uri.fromFile(new File(getBaseContext().getFilesDir().getAbsolutePath() + "/pic_" + ei.id + ".jpg")));
            ei.mapURLSrc = String.valueOf(Uri.fromFile(new File(getBaseContext().getFilesDir().getAbsolutePath() + "/picmap_" + ei.id + ".jpg")));

            events.add(ei);
        }
        this.oldEvents = events;

        List<EventInfo> orderedEvents = CalendarMaker.orderEvents(events);

        this.ei = orderedEvents.get(id);
        parsingCompleted = false;

        netFetchJSON();

    }

    public void netFetchJSON() {
        if (JSONPuller.isNetworkAvailable(this)) {
            // available network
            Log.v("FetchJSON", "Network available");
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL("http://development.studiopresent.info/eventsapp/get-data/json");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setReadTimeout(100000);
                        conn.setConnectTimeout(150000);
                        conn.setRequestMethod("GET");
                        conn.setDoInput(true);
                        // Starts the query
                        conn.connect();
                        InputStream stream = conn.getInputStream();
                        String data = convertStreamToString(stream);
                        readNewJSON(0, data);

                        stream.close();
                        conn.disconnect();

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
        }
    }

    static String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public void sendNotification(String id, EventInfo ei, boolean isUpdate) {
        NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        int id_code = Integer.parseInt(id);

        // Read data from file

        Intent intent = new Intent(this, DetailsActivity.class);
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

        PendingIntent contentIntent;


        String notiText;
        if (isUpdate) {
            notiText = "Event updated, starts at " + ei.dStartDate.getClockTime();
            contentIntent = PendingIntent.getActivity(this, id_code,
                    intent, PendingIntent.FLAG_ONE_SHOT);

        } else {
            notiText = "Event has been canceled";
            contentIntent = PendingIntent.getActivity(this, id_code,
                    intent, PendingIntent.FLAG_NO_CREATE);
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.events_white)
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

    private class CheckDeleteTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            for (EventInfo olditem : oldEvents) {
//                Log.v("DeleteCheck", "Olditem: " + String.valueOf(olditem.nid));
                boolean hasMissing = true;

                for (EventInfo newitem : events) {
//                    Log.v("DeleteCheck", "Newitem: " + String.valueOf(newitem.nid));

                    if (olditem.nid == newitem.nid) {
//                        Log.v("DeleteCheck", "Same item found " + olditem.nid + " " + olditem.title);
                        hasMissing = false;
                    }
                }

                if (hasMissing) {
                    Log.v("DeleteCheck", "Item missing: " + olditem.nid + " " + olditem.title);
                    sendNotification(String.valueOf(olditem.id), olditem, false);
                }
            }
            return null;
        }

    }

    private class CheckNewTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            events = CalendarMaker.orderEvents(events);

            for (EventInfo newitem : events) {
//                Log.v("DeleteCheck", "Olditem: " + String.valueOf(olditem.nid));
                boolean hasMissing = true;

                for (EventInfo olditem : oldEvents) {
//                    Log.v("DeleteCheck", "Newitem: " + String.valueOf(newitem.nid));

                    if (newitem.nid == olditem.nid) {
//                        Log.v("DeleteCheck", "Same item found " + olditem.nid + " " + olditem.title);
                        hasMissing = false;
                    }
                }

                if (hasMissing) {
                    Log.v("NewCheck", "Item missing: " + newitem.nid + " " + newitem.title);
                    sendNotificationNewEvent(String.valueOf(newitem.id), newitem, true);
                }
            }
            return null;
        }

    }


    public void sendNotificationNewEvent(String id, EventInfo ei, boolean isUpdate) {
        NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        int id_code = Integer.parseInt(id);

        // Read data from file

        Intent intent = new Intent(this, MainActivity.class);


        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent;


        String notiText;
        if (isUpdate) {
            notiText = "New event: " + ei.title;
            contentIntent = PendingIntent.getActivity(this, id_code,
                    intent, PendingIntent.FLAG_ONE_SHOT);

        } else {
            notiText = "Event has been canceled";
            contentIntent = PendingIntent.getActivity(this, id_code,
                    intent, PendingIntent.FLAG_NO_CREATE);
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.events_white)
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

}
