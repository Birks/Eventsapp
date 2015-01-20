package com.studiopresent.eventsapp;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    JSONPuller obj;

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
        CardAdapter ca = new CardAdapter(createList());
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
            result.add(ei);

        }

        return result;

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
