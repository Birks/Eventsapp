package com.studiopresent.eventsapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

/**
 * When the alarm fires, this WakefulBroadcastReceiver receives the broadcast Intent
 * and then starts the IntentService {@code SampleSchedulingService} to do some work.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {
    // The app's AlarmManager, which provides access to the system alarm services.
    private AlarmManager alarmMgr;
    // The pending intent that is triggered when the alarm fires.
    private PendingIntent alarmIntent;


    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, SchedulingService.class);
        // Forward to the notification
        service.putExtra("id", intent.getStringExtra("id"));


        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, service);
    }

    /**
     * Sets a repeating alarm that runs once a day at approximately 8:30 a.m. When the
     * alarm fires, the app broadcasts an Intent to this WakefulBroadcastReceiver.
     *
     * @param context
     */
    public void setAlarm(Context context, EventInfo eventInfo) {

        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        // Send some data to onReceive first then to the notification


        intent.putExtra("id", String.valueOf(eventInfo.id));
//        intent.putExtra("clock", eventInfo.dStartDate.hour + ":" + eventInfo.dStartDate.minute);


        alarmIntent = PendingIntent.getBroadcast(context, eventInfo.id, intent, 0);


        Calendar calendar = Calendar.getInstance();
        // Set the alarms trigger time to 8:30 a.m.
        calendar.set(Calendar.YEAR, Integer.parseInt(eventInfo.dStartDate.year));
        calendar.set(Calendar.MONTH, Integer.parseInt(eventInfo.dStartDate.month) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(eventInfo.dStartDate.day));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(eventInfo.dStartDate.hour));
        calendar.add(Calendar.HOUR, -1);
        calendar.set(Calendar.MINUTE, Integer.parseInt(eventInfo.dStartDate.minute));

        //Log.v("Alarm", "Calendar: " + calendar.getTime());


        // Set the alarm to fire at approximately 8:30 a.m., according to the device's clock
        // Check does the event older than the current time
        if (calendar.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()) {
            Log.v("Alarm", "Alarm set " + eventInfo.title + " id: " + eventInfo.id);
            alarmMgr.set(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), alarmIntent);
        }


        // Enable {@code SampleBootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    /**
     * Cancels the alarm.
     *
     * @param context
     */
    public void cancelAlarm(Context context) {
        // If the alarm has been set, cancel it.
        if (alarmMgr != null) {
            alarmMgr.cancel(alarmIntent);
        }

        // Disable {@code SampleBootReceiver} so that it doesn't automatically restart the 
        // alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
