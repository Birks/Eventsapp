package com.studiopresent.eventsapp;


import android.util.Log;

import java.util.Collections;
import java.util.List;


/**
 * Project Eventsapp
 * Created by Tamás on 3/29/2015.
 */
public class CalendarMaker {

    public static DateObject generateFromString(String stringDate) {
        DateObject mDateObject = new DateObject();
        Log.v("Date", "Month: " + stringDate.substring(0, 2));
        mDateObject.month = stringDate.substring(0, 2);
        Log.v("Date", "Day: " + stringDate.substring(3, 5));
        mDateObject.day = stringDate.substring(3, 5);
        Log.v("Date", "Year: " + stringDate.substring(6, 10));
        mDateObject.year = stringDate.substring(6, 10);
        Log.v("Date", "Hour: " + stringDate.substring(13, 15));
        mDateObject.hour = stringDate.substring(13, 15);
        Log.v("Date", "Minute: " + stringDate.substring(16, 18));
        mDateObject.minute = stringDate.substring(16, 18);

        return mDateObject;
    }

    public static List<EventInfo> orderEvents(List<EventInfo> events) {
        int temp;
        for (int i = 0; i < events.size() - 1; i++) {
            for (int j = 1; j < events.size() - i; j++) {
                if (Long.parseLong(events.get(j - 1).dStartDate.toString()) > Long.parseLong(events.get(j).dStartDate.toString())) {
                    Collections.swap(events, j - 1, j);

                    // Because it showed the old event on onclick
                    temp = events.get(j - 1).id;
                    events.get(j - 1).id = events.get(j).id;
                    events.get(j).id = temp;
                }
            }
        }

        return events;
    }

}
