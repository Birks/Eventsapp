package com.studiopresent.eventsapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

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
        service.putExtra("title", intent.getStringExtra("title"));


        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, service);
    }

    /**
     * Sets a repeating alarm that runs once a day at approximately 8:30 a.m. When the
     * alarm fires, the app broadcasts an Intent to this WakefulBroadcastReceiver.
     *
     * @param context
     */
    public void setAlarm(Context context, DateObject schedulingDate, String title) {
        Log.v("Alarm", "schedulingDate title: " + title);
        Log.v("Alarm", "schedulingDate year: " + schedulingDate.year);
        Log.v("Alarm", "schedulingDate month: " + schedulingDate.month);
        Log.v("Alarm", "schedulingDate day: " + schedulingDate.day);
        Log.v("Alarm", "schedulingDate hour: " + schedulingDate.hour);
        Log.v("Alarm", "schedulingDate minute: " + schedulingDate.minute);
        //this.title=title;

        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("title", title);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
        // Set the alarm's trigger time to 8:30 a.m.
        calendar.set(Calendar.YEAR, Integer.parseInt(schedulingDate.year));
        calendar.set(Calendar.MONTH, Integer.parseInt(schedulingDate.month)-1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(schedulingDate.day));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(schedulingDate.hour));
        calendar.add(Calendar.HOUR, -1);
        calendar.set(Calendar.MINUTE, Integer.parseInt(schedulingDate.minute));

//        calendar.set(Calendar.HOUR_OF_DAY, 18);
//        calendar.set(Calendar.MINUTE, 20);

        Log.v("Alarm", "Calendar: " + calendar.getTime());
        //Log.v("Alarm", "Alarm set");


        // Set the alarm to fire at approximately 8:30 a.m., according to the device's
        // clock, and to repeat once a day.
        if (calendar.getTimeInMillis()>Calendar.getInstance().getTimeInMillis()) {
            Log.v("Alarm", "Alarm set " + title + " Current time: " + Calendar.getInstance().getTime());
            alarmMgr.set(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), alarmIntent);
//            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
//                    calendar.getTimeInMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);
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
