package com.studiopresent.eventsapp;

import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.card_list);

        // use this setting to improve performance if you know that changes
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager mlinearLayoutManager = new LinearLayoutManager(this);
        mlinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mlinearLayoutManager);

        // specify an adapter
        events=createList();
        CardAdapter ca = new CardAdapter(events);
        mRecyclerView.setAdapter(ca);

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
            ei.img=obj.getImg();
            ei.eventId=i;

            // OnlClickListener added for the dynamic rlayout onClick function
            ei.onClickListener = new View.OnClickListener() {
                public void onClick(View v) {
                    Log.v("openEvent", String.valueOf(v.getId()));
                    openEvent(v.getId());
                }};

            result.add(ei);

        }

        return result;

    }

    // TODO send all the data to the other obj
    // This function opens the new Activity
    public void openEvent(int index) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("INDEX", Integer.toString(index));
        intent.putExtra("TITLE", events.get(index).title);
        intent.putExtra("STARTDATE", events.get(index).startDate);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
