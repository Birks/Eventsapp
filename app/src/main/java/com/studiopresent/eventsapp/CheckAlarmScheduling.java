package com.studiopresent.eventsapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class CheckAlarmScheduling extends IntentService implements AsyncTaskCompleteListener<String> {
    public CheckAlarmScheduling() {
        super("CheckAlarmScheduling");
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v("Alarm", "Alarm wake received check");

        // Sends the notification only when there is only a new question


        AlarmReceiver.completeWakefulIntent(intent);
    }


    @Override
    public void onTaskComplete(String result) {


    }

    @Override
    public void onRefreshTaskComplete(String result) {

    }

}
