package com.studiopresent.eventsapp;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.studiopresent.eventsapp.gson.EventsJson;

import java.io.File;

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

    public SchedulingService() {
        super("SchedulingService");
    }

    // An ID used to post the notification.
    public static final int NOTIFICATION_ID = 1;

    @Override
    protected void onHandleIntent(Intent intent) {

//        Log.v("Alarm", "IntentExtra title: " + intent.getStringExtra("title"));
        Log.v("Alarm", "IntentExtra id: " + intent.getStringExtra("id"));
        // The URL from which to fetch content.
        sendNotification(intent.getStringExtra("id"));

        Log.v("Alarm", "Notification received!");

        // Release the wake lock provided by the BroadcastReceiver.
        AlarmReceiver.completeWakefulIntent(intent);

    }

    // Post a notification indicating whether a doodle was found.
    private void sendNotification(String id) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        int id_code = Integer.parseInt(id);

        // Read data from file
        readJSON(id_code);
        while (parsingCompleted) ;

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

        PendingIntent contentIntent = PendingIntent.getActivity(this, id_code,
                intent, PendingIntent.FLAG_ONE_SHOT);

        String notiText="Starts at " + ei.dStartDate.getClockTime();


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
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

    public void readJSON(int id) {
        FileSaveMethods fileSaveMethods = new FileSaveMethods(this);

        // GSON initalize
        Gson gson = new GsonBuilder().create();
        EventsJson gObj = gson.fromJson(fileSaveMethods.readFromFile("json_string"), EventsJson.class);
        ei = new EventInfo();

        ei.title = gObj.nodes[id].node.title;

        ei.dStartDate = CalendarMaker.generateFromString(gObj.nodes[id].node.startDate);
        ei.startDate = ei.dStartDate.getSerbianDateFormat();

        ei.updatedDate = gObj.nodes[id].node.updatedDate;

        ei.endDate = gObj.nodes[id].node.endDate;
        ei.body = gObj.nodes[id].node.body;
        ei.name = gObj.nodes[id].node.name;

        ei.city = gObj.nodes[id].node.city;
        ei.street = gObj.nodes[id].node.street;
        ei.latitude = gObj.nodes[id].node.latitude;
        ei.longitude = gObj.nodes[id].node.longitude;

        ei.id = id;

        ei.imageSrc = gObj.nodes[id].node.imageHdpi.src;


        ei.imageSrc = String.valueOf(Uri.fromFile(new File(getBaseContext().getFilesDir().getAbsolutePath() + "/pic_" + ei.id + ".jpg")));
        ei.mapURLSrc = String.valueOf(Uri.fromFile(new File(getBaseContext().getFilesDir().getAbsolutePath() + "/picmap_" + ei.id + ".jpg")));

        parsingCompleted = false;

    }


}
