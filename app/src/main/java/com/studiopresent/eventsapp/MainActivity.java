package com.studiopresent.eventsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

import java.util.List;


public class MainActivity extends ActionBarActivity {

    JSONPuller obj;
    List<EventInfo> events;
    List<EventInfo> unorderedEvents;
    SwipeRefreshLayout mSwipeRefreshLayout;
    FileSaveMethods fileManager;
    ProgressBar progressbar;

    boolean resetComplete = true;

    //A ProgressDialog object
    //private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Dynamic RelativeLayout generation is moved in the onPostExecute method

        // Instantiation of the JSONPuller object
        obj = new JSONPuller(this, getBaseContext());

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // Initialize file manager
        fileManager = new FileSaveMethods(getApplicationContext());

        // Splash screen visible
        setContentView(R.layout.splash_screen_layout);

        //Check does the file exists on first run after install
        if (!fileManager.fileExists("json_string") && !JSONPuller.isNetworkAvailable(this)) {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Error");
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setMessage("Can't reach the server");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
            alertDialog.show();
        } else {

            // Calling the AsyncTask
            new LoadViewTask().execute();
        }
    }


    // To use the AsyncTask, it must be subclassed
    private class LoadViewTask extends AsyncTask<Void, Integer, Void> {
        // Before running code in the separate thread
        @Override
        protected void onPreExecute() {
            progressbar = (ProgressBar) findViewById(R.id.progressBar);
            progressbar.setVisibility(View.VISIBLE);

        }

        // The code to be executed in a background thread.
        @Override
        protected Void doInBackground(Void... params) {
            /* This is the part where the JSON parsing called, and this part
             * fills the List<EventInfo> events with event objects
			 */
            //Get the current thread's token
            synchronized (this) {
                // Calling the JSON Pulling action
                obj.newFetchJSON(false);
            }

            return null;
        }
    }

    protected boolean enabled = true;

    public void enable(boolean b) {
        enabled = b;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (enabled)
            return super.dispatchTouchEvent(ev);
        return true;
    }

    // AsyncTask which needed for the SwipeRefresh
    private class RefreshItems extends AsyncTask<Void, Integer, Void> {
        // The code to be executed in a background thread.
        @Override
        protected Void doInBackground(Void... params) {

            if (!JSONPuller.isNetworkAvailable(MainActivity.this)) {
                // If not connected then show an Alertdialog
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog.setTitle("Error");
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.setMessage("Can't reach the server");
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            } else {
                enable(false);
                // Starting new JSON pulling
                resetAllAlarm();

                events.clear();
                obj = new JSONPuller(MainActivity.this, getBaseContext());
                obj.newFetchJSON(true);
            }
            return null;
        }

    }

    // When swipe refresh and first run ends
    public void connectWithRecycleVIew() {

        // initialize the View
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.card_list);

        // use this setting to improve performance if you know that changes
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager mlinearLayoutManager = new LinearLayoutManager(MainActivity.this);
        mlinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mlinearLayoutManager);

        // specify an adapter
        CardAdapter ca = new CardAdapter(events, getBaseContext());
        mRecyclerView.setAdapter(ca);


    }

    // This function opens the new Activity
    public void openEvent(int index) {
        // Packing the event data in the intent
        Intent intent = new Intent(this, DetailsActivity.class);

        Log.v("Intent", "Index: " + index);
        // This index is just temporary used.
        intent.putExtra("INDEX", Integer.toString(index));

        // Image intent
        intent.putExtra("BITMAP", events.get(index).imageSrc);

        intent.putExtra("TITLE", events.get(index).title);
        intent.putExtra("STARTDATE", events.get(index).startDate);
        intent.putExtra("NAME", events.get(index).name);
        intent.putExtra("BODY", events.get(index).body);

        intent.putExtra("CITY", events.get(index).city);
        intent.putExtra("STREET", events.get(index).street);

        intent.putExtra("LATITUDE", events.get(index).latitude);
        intent.putExtra("LONGITUDE", events.get(index).longitude);

        String GPS = events.get(index).latitude + ", " + events.get(index).longitude;
        intent.putExtra("GPS", GPS);

        intent.putExtra("MAPURL", events.get(index).mapURLSrc);
        startActivity(intent);
    }

    public void createAlarm() {
        for (int i = 0; i < events.size(); i++) {
            Log.v("Alarm", "Trying to create alarm");
            AlarmReceiver ar = new AlarmReceiver();
            ar.setAlarm(this, events.get(i));
        }
    }

    public void resetAllAlarm() {
        for (int i = 0; i < events.size(); i++) {
            Log.v("Alarm", "Trying to cancel alarm " + i);
            AlarmReceiver ar = new AlarmReceiver();
            ar.setAlarm(this, events.get(i));
            ar.cancelAlarm(this);
        }
        resetComplete = false;

    }

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("ID")) {
                Log.v("Startup", extras.getString("ID"));
                openEvent(Integer.parseInt(extras.getString("ID")));
            }
        }
    }


    public void onTaskComplete() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onParseComplete();

            }
        });

    }

    public void onRefreshTaskComplete() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onRefreshParseComplete();

            }
        });
    }

    public void onRefreshParseComplete() {
        Log.v("Startup", "onRefreshParseComplete");
        enable(true);

        events = CalendarMaker.orderEvents(obj.getEvents());
        createAlarm();
        connectWithRecycleVIew();
        // Finish the refreshing spinner
        mSwipeRefreshLayout.setRefreshing(false);
    }


    public void onParseComplete() {
        Log.v("Startup", "onParseComplete");

        // Getting the List<EventInfo> array
        events = obj.getEvents();
        unorderedEvents = obj.getEvents();
        onNewIntent(getIntent());
        events = CalendarMaker.orderEvents(obj.getEvents());
        createAlarm();

        progressbar.setVisibility(View.GONE);

        // The onCreate part.
        setContentView(R.layout.activity_main);

        // NOTE: everything from here was moved in the method connectWithRecycleVIew()
        connectWithRecycleVIew();

        // Must be here because in onCreate the app crashes
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.dark_green);
        // This part is needed for the pull to refresh
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                Log.v("MainActivity", "Refreshing Items");
                // Calling the JSON Pulling action if the device is connected to the internet
                new RefreshItems().execute();
            }
        });


    }

    /*@Override
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
    }*/
}
