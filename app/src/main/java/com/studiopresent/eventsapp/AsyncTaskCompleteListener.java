package com.studiopresent.eventsapp;

public interface AsyncTaskCompleteListener <T> {
    public void onTaskComplete(T result);
    public void onRefreshTaskComplete(T result);
}
