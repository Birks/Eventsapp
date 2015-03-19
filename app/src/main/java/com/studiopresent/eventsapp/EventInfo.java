package com.studiopresent.eventsapp;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;

/*
 *   This class contains the data which is displayed on the main screen
 */

public class EventInfo {

    // TODO: not containing the full data yet
    protected int id;
    protected int nid;
    protected View.OnClickListener onClickListener;
    protected String title;
    protected String startDate;
    protected String endDate;
    protected String postDate;
    protected String updatedDate;
    protected String body;
    protected String city;
    protected double latitude;
    protected double longitude;
    protected String name;
    protected String street;
    protected Drawable imageHdpi;
    protected Bitmap imageBitmap;
}
