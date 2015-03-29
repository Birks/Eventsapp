package com.studiopresent.eventsapp;

/**
 * Project Eventsapp
 * Created by Tamás on 3/29/2015.
 */
public class DateObject {

    public String year;
    public String month;
    public String day;
    public String hour;
    public String minute;

    public String getSerbianDateFormat() {
        return day + "." + month + "." + year + ". - " + hour + ":" + minute;
    }

    public String toString() {
        return year + month + day + hour + minute;
    }
}
