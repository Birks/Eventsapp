package com.studiopresent.eventsapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;


/* Details page for the event */

public class DetailsActivity extends ActionBarActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Intent intent = getIntent();
        String message = intent.getStringExtra("INDEX");
        //TODO optimise this part
        // Create the text view
        TextView textView;// = (TextView) findViewById(R.id.det_index);
        //textView.setText(message);


        message = intent.getStringExtra("TITLE");
        textView = (TextView) findViewById(R.id.det_title);
        textView.setText(message);

        message = intent.getStringExtra("STARTDATE");
        textView = (TextView) findViewById(R.id.det_startdate);
        textView.setText(message);

        /*message = intent.getStringExtra("ENDDATE");
        textView = (TextView) findViewById(R.id.det_enddate);
        textView.setText(message);*/

        message = intent.getStringExtra("BODY");
        textView = (TextView) findViewById(R.id.det_body);
        textView.setText(message);

        // IMAGE
        byte[] byteArray = getIntent().getByteArrayExtra("BITMAP");
        Log.v("bitmap", "receive BITMAP intent");
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        Log.v("bitmap", "bitmap factory");
        ImageView imageView = (ImageView) findViewById(R.id.det_image);
        Log.v("bitmap", "imageview initialized");
        imageView.setImageBitmap(bitmap);
        Log.v("bitmap", "bitmap added imageview");

        // NAME(Korzo)
        message = intent.getStringExtra("NAME");
        textView = (TextView) findViewById(R.id.det_name);
        textView.setText(message);

        // CITY, STREET, GPS
        message = intent.getStringExtra("CITY");
        textView = (TextView) findViewById(R.id.det_city);
        textView.setText("Grad: "+message);

        message = intent.getStringExtra("STREET");
        textView = (TextView) findViewById(R.id.det_street);
        textView.setText("Ulica: "+message);

        message = intent.getStringExtra("GPS");
        textView = (TextView) findViewById(R.id.det_GPS);
        textView.setText("GPS: "+message);
        String defaultMap = "http://maps.google.com/maps/api/staticmap?center=";
        String defaultMap2 = "&zoom=16&size=400x230&sensor=false&markers=";


        String coord = "";
        message = intent.getStringExtra("LATITUDE");
        coord += message + "%2C";
        message = intent.getStringExtra("LONGITUDE");
        coord += message;
        defaultMap += coord + defaultMap2 + coord;
        Log.v("coord", defaultMap);
        ImageView detMap = (ImageView) findViewById(R.id.det_map);
        Picasso.with(this).load(defaultMap).placeholder(R.drawable.ic_file_download_white_48dp).into(detMap);
    }

    public void Back(View v){
        onBackPressed();
    }

    // This part is only required for a later use. Not functional yet.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
