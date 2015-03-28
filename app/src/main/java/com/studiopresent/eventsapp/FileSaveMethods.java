package com.studiopresent.eventsapp;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class FileSaveMethods {

    Context fileContext;

    // Constructor
    public FileSaveMethods(Context fileContext) {
        this.fileContext = fileContext;
    }

    public String readFromFile(String FILENAME) {
        FileInputStream fis;
        try {
            fis = fileContext.getApplicationContext().openFileInput(FILENAME);

            String result = "";
            int content;
            while ((content = fis.read()) != -1) {
                // convert to char and display it
                result += String.valueOf((char) content);
            }
            Log.v("FIS", result);
            return result;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void saveToFile(String FILENAME, String string) {
        FileOutputStream fos;
        try {
            fos = fileContext.getApplicationContext().openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(string.getBytes());
            fos.close();
            Log.v("FOS", "Created file:"+ FILENAME + ", Value: " + string);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.v("FOS", "Write Exception");
            e.printStackTrace();
        }
    }


    public boolean fileExists(String filename) {
        File file = fileContext.getFileStreamPath(filename);
        return !(file == null || !file.exists());
    }
}
