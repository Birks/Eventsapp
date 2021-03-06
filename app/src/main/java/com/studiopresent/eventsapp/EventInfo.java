package com.studiopresent.eventsapp;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.squareup.picasso.Picasso;

import java.util.Calendar;

/*
 *   This class contains the data which is displayed on the main screen
 */

public class EventInfo {

    // TODO: not containing the full data yet
    protected int id;
    protected int nid;
    protected View.OnClickListener onClickListener;
    protected String title;
    protected DateObject dStartDate;
    protected String startDate;
    protected String endDate;
    protected String postDate;
    protected String updatedDate;
    protected String body;
    protected String city;
    protected String latitude;
    protected String longitude;
    protected String name;
    protected String street;
    protected String imageSrc;
    protected Drawable imageHdpi;
    protected Bitmap imageBitmap;
    protected Picasso pic;
    protected String mapURL;
    protected Bitmap mapBitmap;
    protected String mapURLSrc;
}
