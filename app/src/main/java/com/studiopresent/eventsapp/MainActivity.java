package com.studiopresent.eventsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
        //setContentView(R.layout.activity_main);

        // Everything from the onCreate method moved to the AsyncTasks onPostExecute method

        // Calling hte AsyncTask
        new LoadViewTask().execute();
    }

    private List<EventInfo> createList() {

        List<EventInfo> result = new ArrayList<EventInfo>();

        // JSON Puller part
        // TODO: need to be optimized

        // Get the Array length
        obj = new JSONPuller(0);
        obj.fetchJSON();

        while(obj.parsingComplete);
        int size = obj.getNodes().length();


        for (int i = 0; i < size; i++) {

            obj = new JSONPuller(i);
            obj.fetchJSON();

            while(obj.parsingComplete);
            EventInfo ei = new EventInfo();
            ei.title= obj.getjTitle();
            ei.startDate=obj.getjStartDate();
            ei.endDate=obj.getjEndDate();
            ei.imageHdpi =obj.getImageHdpi();
            ei.id=i;
            ei.body=obj.getjBody();

            // OnlClickListener added for the dynamic rlayout onClick function
            // An ID is given to every card, and at onlcick the detailsactivity is opened with a given ID
            ei.onClickListener = new View.OnClickListener() {
                public void onClick(View v) {
                    Log.v("openEvent", String.valueOf(v.getId()));
                    openEvent(v.getId());
                }};

            result.add(ei);

        }

        return result;

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

                    events=createList();
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
        intent.putExtra("TITLE", events.get(index).title);
        intent.putExtra("STARTDATE", events.get(index).startDate);
        intent.putExtra("ENDDATE", events.get(index).endDate);
        intent.putExtra("BODY", events.get(index).body);
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
