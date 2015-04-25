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

/**
 * This {@code IntentService} does the app's actual work.
 * {@code SampleAlarmReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class SchedulingService extends IntentService {

    public SchedulingService() {
        super("SchedulingService");
        //this.title=intent.getStringExtra("title");
    }

    private String title;

    // An ID used to post the notification.
    public static final int NOTIFICATION_ID = 1;

    @Override
    protected void onHandleIntent(Intent intent) {


        Log.v("Alarm","IntentExtra title: " + intent.getStringExtra("title"));
        Log.v("Alarm","IntentExtra id: " + intent.getStringExtra("id"));
        Log.v("Alarm","IntentExtra clock:" + intent.getStringExtra("clock"));
        // The URL from which to fetch content.

        sendNotification(
                intent.getStringExtra("title"),
                intent.getByteArrayExtra("bitmap"),
                intent.getStringExtra("startDate"),
                intent.getStringExtra("endDate"),
                intent.getStringExtra("name"),
                intent.getStringExtra("body"),
                intent.getStringExtra("city"),
                intent.getStringExtra("street"),
                intent.getStringExtra("latitude"),
                intent.getStringExtra("longitude"),
                intent.getStringExtra("gps"),
                intent.getStringExtra("mapUrlSrc"),
                intent.getStringExtra("id"),
                intent.getStringExtra("clock"));

        Log.i("Alarm", "Notification received!");


        // Release the wake lock provided by the BroadcastReceiver.
        AlarmReceiver.completeWakefulIntent(intent);

    }

    // Post a notification indicating whether a doodle was found.
    private void sendNotification(String title,
                                  byte[] byteArray,
                                  String startDate,
                                  String endDate,
                                  String name,
                                  String body,
                                  String city,
                                  String street,
                                  String latitude,
                                  String longitude,
                                  String gps,
                                  String mapUrlSrc,
                                  String id,
                                  String clock) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent =  new Intent(this, DetailsActivity.class);
        intent.putExtra("ID", id);
        intent.putExtra("BITMAP", byteArray);
        intent.putExtra("TITLE", title);
        intent.putExtra("STARTDATE", startDate);
        intent.putExtra("NAME", name);
        intent.putExtra("BODY", body);
        intent.putExtra("CITY", city);
        intent.putExtra("STREET", street);
        intent.putExtra("LATITUDE", latitude);
        intent.putExtra("LONGITUDE", longitude);
        intent.putExtra("GPS", gps);
        intent.putExtra("MAPURL", mapUrlSrc);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        int id_code = Integer.parseInt(id);
        PendingIntent contentIntent = PendingIntent.getActivity(this, id_code,
                intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Starts at " +clock))
                        .setContentText("Starts at " + id);

        mBuilder.setAutoCancel(true);
        mBuilder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;


        // Set a sound effect to the notification
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(Integer.parseInt(id), mBuilder.build());
    }

}
