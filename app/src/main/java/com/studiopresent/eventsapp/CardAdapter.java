package com.studiopresent.eventsapp;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;



/*
    Adapters provide a binding from an app-specific data set to views that are displayed within a RecyclerView.
    This adapter contains a static class which is needed for the binding.
 */

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.EventCardHolder> {

    private List<EventInfo> eventsList;
    private Context context;

    // CardAdapter constructor
    public CardAdapter(List<EventInfo> eventsList, Context context) {
        this.context = context;
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
    public void onBindViewHolder(final EventCardHolder holder, int i) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        EventInfo ei = eventsList.get(i);

        holder.vTitle.setText(ei.title);
        holder.vName.setText(ei.name);
        holder.vSTartDate.setText(ei.startDate);
        holder.rlayout.setId(ei.id);
        holder.rlayout.setOnClickListener(ei.onClickListener);

        // Experimental
//        holder.vTint.setBackgroundColor(context.getResources().getIntArray(R.array.material_colors)
//                [ei.nid % context.getResources().getIntArray(R.array.material_colors).length]);

        Picasso mPic = Picasso.with(context);
        //mPic.setIndicatorsEnabled(true);
        //mPic.setLoggingEnabled(true);
        // Shows the progressbar spinner until the image downloaded
        mPic.load(ei.imageSrc).noPlaceholder().into(holder.vImage, new Callback() {
            @Override
            public void onSuccess() {
                // Hide the spinner
                holder.vProgress.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                Log.v("Picasso:", "Error");
            }
        });
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return eventsList.size();
    }


    // Provide a reference to the views for each data item
    public static class EventCardHolder extends RecyclerView.ViewHolder {

        protected TextView vTitle;
        protected TextView vSTartDate, vName;
        protected ImageView vImage;
        protected ImageView vTint;
        protected RelativeLayout rlayout; // this needed for the background change
        protected View vProgress;

        public EventCardHolder(View v) {
            super(v);
            vTitle = (TextView) v.findViewById(R.id.txt_title);
            vSTartDate = (TextView) v.findViewById(R.id.txt_startDate);
            vName = (TextView) v.findViewById(R.id.txt_name);
            vTint = (ImageView) v.findViewById(R.id.tint);
            vImage = (ImageView) v.findViewById(R.id.image);
            rlayout = (RelativeLayout) v.findViewById(R.id.card_rlayout);
            vProgress = v.findViewById(R.id.card_progressbar);
        }
    }
}