package com.studiopresent.eventsapp;


import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;


/*
    Adapters provide a binding from an app-specific data set to views that are displayed within a RecyclerView.
    This adapter contains a static class which is needed for the binding.
 */

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.EventCardHolder> {

    private List<EventInfo> eventsList;

    // CardAdapter constructor
    public CardAdapter(List<EventInfo> eventsList) {
        this.eventsList = eventsList;
    }


    // Create new views (invoked by the layout manager)
    @Override
    public EventCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.card_layout, parent, false);

        return new EventCardHolder(itemView);
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(EventCardHolder holder, int i) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        EventInfo ei = eventsList.get(i);

        holder.vTitle.setText(ei.title);
        holder.vSTartDate.setText(ei.startDate);
        holder.rlayout.setId(ei.eventId);
        holder.rlayout.setOnClickListener(ei.onClickListener);
        holder.rlayout.setBackgroundDrawable(ei.img);

    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return eventsList.size();
    }


    // Provide a reference to the views for each data item
    public static class EventCardHolder extends RecyclerView.ViewHolder {

        protected TextView vTitle;
        protected TextView vSTartDate;
        protected RelativeLayout rlayout; // this needed for the background change

        public EventCardHolder(View v) {
            super(v);
            vTitle = (TextView) v.findViewById(R.id.txt_title);
            vSTartDate = (TextView) v.findViewById(R.id.txt_startDate);
            rlayout = (RelativeLayout) v.findViewById(R.id.card_rlayout);
        }
    }
}