package com.studiopresent.eventsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    JSONPuller obj;
    List<EventInfo> events;

    //A ProgressDialog object
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Dynamic RelativeLayout generation is moved in the onPostExecute method

        // Instantiation of the JSONPuller object
        obj = new JSONPuller(this);

        // Calling hte AsyncTask
        new LoadViewTask().execute();
    }


    // To use the AsyncTask, it must be subclassed
    private class LoadViewTask extends AsyncTask<Void, Integer, Void>
    {
        // Before running code in the separate thread
        @Override
        protected void onPreExecute()
        {
            // Create a new progress dialog
            progressDialog = new ProgressDialog(MainActivity.this);
            // Set the progress dialog spinner progress bar
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            // Set the dialog title to 'Loading...'
            progressDialog.setTitle("Loading...");
            // Set the dialog message to 'Loading application View, please wait...'
            progressDialog.setMessage("Loading application, please wait...");
            // This dialog can't be canceled by pressing the back key
            progressDialog.setCancelable(false);
            // This dialog isn't indeterminate
            progressDialog.setIndeterminate(false);
            // The maximum number of items is 100
            progressDialog.setMax(100);
            // Set the current progress to zero
            progressDialog.setProgress(0);
            // Display the progress dialog
            progressDialog.show();
        }

        // The code to be executed in a background thread.
        @Override
        protected Void doInBackground(Void... params)
        {
			/* This is the part where the JSON parsing called, and this part
			 * fills the List<EventInfo> events with event objects
			 */
                //Get the current thread's token
                synchronized (this)
                {
                    // Calling the JSON Pulling action
                    obj.fetchJSON();

                    // Delay until the parsing is completed
                    while (obj.parsingComplete);

                    // Getting the List<EventInfo> array
                    events=obj.getEvents();

                    // This ends the spinner
                    publishProgress(100);
                }

            return null;
        }

        // Update the progress
        @Override
        protected void onProgressUpdate(Integer... values)
        {
            // set the current progress of the progress dialog
            progressDialog.setProgress(values[0]);
        }

        // after executing the code in the thread
        @Override
        protected void onPostExecute(Void result)
        {
            // close the progress dialog
            progressDialog.dismiss();

            // The onCreate part.
            setContentView(R.layout.activity_main);
            // initialize the View
            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.card_list);

            // use this setting to improve performance if you know that changes
            mRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            LinearLayoutManager mlinearLayoutManager = new LinearLayoutManager(MainActivity.this);
            mlinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(mlinearLayoutManager);

            // specify an adapter
            CardAdapter ca = new CardAdapter(events);
            mRecyclerView.setAdapter(ca);

        }
    }



    // TODO send all the data to the other obj
    // This function opens the new Activity
    public void openEvent(int index) {
        // Packing the event data in the intent
        Intent intent = new Intent(this, DetailsActivity.class);

        // This index is just temporary used.
        intent.putExtra("INDEX", Integer.toString(index));

        // Image intent
        Bitmap bitmap = events.get(index).imageBitmap;
        Log.v("bitmap", "bitmap created");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Log.v("bitmap", "stream created");
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        Log.v("bitmap", "bitmap comppress format");
        byte[] byteArray = stream.toByteArray();
        Log.v("bitmap", "byte array created");
        intent.putExtra("BITMAP", byteArray);
        Log.v("bitmap", "BITMAP send with intent");


        intent.putExtra("TITLE", events.get(index).title);
        intent.putExtra("STARTDATE", events.get(index).startDate);
        intent.putExtra("NAME", events.get(index).name);
        intent.putExtra("BODY", events.get(index).body);

        intent.putExtra("CITY", events.get(index).city);
        intent.putExtra("STREET", events.get(index).street);

        String GPS = events.get(index).latitude + ", " + events.get(index).longitude;
        intent.putExtra("GPS", GPS);
        startActivity(intent);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
