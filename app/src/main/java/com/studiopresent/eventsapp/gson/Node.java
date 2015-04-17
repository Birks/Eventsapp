package com.studiopresent.eventsapp.gson;

/**
 * Project name: gyakorlo
 * Created by Tam�s on 4/16/2015.
 */
public class Node {
    public String title;
    public String startDate;
    public String endDate;
    public String body;
    public String name;
    public String city;
    public String street;
    public String longitude;
    public String latitude;

    public ImageHdpi imageHdpi;

    @Override
    public String toString() {
        return " title - " + title + "\n startDate - " + startDate + "\n endDate - " + endDate + "" +
                "\n body - " + body + "\n" + imageHdpi + "\n name - " + name + "\n city - " + city +
                "\n street - " + street + "\n longitude - " + longitude + "\n latitude - " + latitude;

    }
}
