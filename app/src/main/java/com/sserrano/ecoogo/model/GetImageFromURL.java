package com.sserrano.ecoogo.model;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import com.sserrano.ecoogo.model.interfaces.ImageFromURLReceiver;

import java.io.InputStream;
import java.net.URL;

public class GetImageFromURL extends AsyncTask<String, Void, Drawable> {

    public static final String TAG = GetImageFromURL.class.getSimpleName();

    private ImageFromURLReceiver receiver = null;

    @Override
    protected Drawable doInBackground(String... params) {
        try {
            if(params != null){
                InputStream inputStream = (InputStream) new URL(params[0]).getContent();
                return Drawable.createFromStream(inputStream, "imageURL");
            } else {
                return null;
            }

        } catch (Exception e){
            Log.w(TAG, "An exception was thrown while trying to get the business image " +
                    "from the imageURL attribute. Returning null.");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Drawable drawable) {
        if(receiver != null) receiver.onImageReceived(drawable);
    }

    public void setReceiver(ImageFromURLReceiver receiver) {
        this.receiver = receiver;
    }
}
