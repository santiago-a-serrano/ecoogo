package com.sserrano.ecoogo.model;

import androidx.fragment.app.FragmentActivity;
import android.util.Log;


import com.sserrano.ecoogo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CloudData {

    public static final String TAG = CloudData.class.getSimpleName();

    private String jsonURL = "https://santiago-a-serrano.github.io/ecoogo.json";
    private String jsonData;
    private boolean dataAvailable = false, failed = false;
    private JSONArray types, tags;
    private Businesses businesses;
    private FragmentActivity caller;



    public CloudData(FragmentActivity caller){
        this.caller = caller;
        
        OkHttpClient client = new OkHttpClient.Builder().
                connectTimeout(15, TimeUnit.SECONDS).
                readTimeout(15, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(jsonURL).build();
        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onJSONFailed(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                onJSONObtained(response);
            }
        });
    }

    public boolean didFailGettingData() {
        return failed;
    }

    public boolean isDataAvailable(){
        return dataAvailable;
    }

    public Businesses getBusinesses() {
        logIfDataNotAvailable("getBusinesses()");
        return businesses;
    }

    private void onJSONFailed(Exception e){
        Log.d(TAG, "JSON Failed: " + e);
        failed = true;
    }

    private void onJSONObtained(Response response) throws IOException {
        jsonData = response.body().string();
        Log.d(TAG, "JSON data received: " + jsonData);

        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray businessesJSONArray = jsonObject.getJSONArray("businesses");
            types = jsonObject.getJSONArray("businessTypes");
            tags = jsonObject.getJSONArray("ecoTags");
            businesses = jsonToBusinesses(businessesJSONArray);

            dataAvailable = true;
        } catch (JSONException e){
            Log.w(TAG, "JSON Exception: " + e);
            onJSONFailed(e);
        }
    }

    private int[] intJSONArrayToIntArray(JSONArray jsonArray) throws JSONException{
        int[] result = new int[jsonArray.length()];
        for(int i = 0; i < jsonArray.length(); i++){
            result[i] = jsonArray.getInt(i);
        }
        return result;
    }

    private String[] stringJSONArrayToStringArray(JSONArray jsonArray) throws JSONException{
        String[] result = new String[jsonArray.length()];
        for(int i = 0; i < jsonArray.length(); i++){
            result[i] = jsonArray.getString(i);
        }
        return result;
    }

    private Businesses jsonToBusinesses(JSONArray jsonArray) throws JSONException{
        Business[] businessArray = new Business[jsonArray.length()];
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject object = jsonArray.getJSONObject(i);

            int type = object.getInt("type");
            int[] tags = intJSONArrayToIntArray(object.getJSONArray("tags"));


            String typeName =
                    types.getJSONObject(type).getString(caller.getString(R.string.language_code));
            String[] tagNames = new String[tags.length];
            for(int j = 0; j < tags.length; j++){
                tagNames[j] = this.tags.getJSONObject(tags[j]).getString
                        (caller.getString(R.string.language_code));
            }

            Business business = new Business(object.getString("name"), type, typeName,
                    tags, tagNames, object.getDouble("latitude"),
                    object.getDouble("longitude"), object.getString("address"),
                    object.getInt("rating"));

            if(!object.isNull("hours"))
                business.setHours(object.getString("hours"));
            if(!object.isNull("webpage"))
                business.setWebpage(object.getString("webpage"));
            if(!object.isNull("phone"))
                business.setPhone(object.getString("phone"));
            if(!object.isNull("images"))
                business.setImageURLs
                        (stringJSONArrayToStringArray(object.getJSONArray("images")));


            businessArray[i] = business;
        }
        return new Businesses(businessArray, jsonToBusinessTypes(types), jsonToTags(tags));
    }


    private BusinessType[] jsonToBusinessTypes(JSONArray jsonArray) throws JSONException{
        BusinessType[] result = new BusinessType[jsonArray.length()];
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject typeObject = jsonArray.getJSONObject(i);
            result[i] = new BusinessType(typeObject.getString(
                    caller.getString(R.string.language_code) + "plural"), i);
        }

        return result;
    }

    private Tag[] jsonToTags(JSONArray jsonArray) throws JSONException{
        Tag[] result = new Tag[jsonArray.length()];
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject tagObject = jsonArray.getJSONObject(i);
            result[i] = new Tag(tagObject.getString(caller.getString(R.string.language_code)));
        }

        return result;
    }

    private void logIfDataNotAvailable(String methodCalling){
        if(!dataAvailable) {
            Log.w(TAG, methodCalling + "method was called in a moment where data was not " +
                    "available. A null array/object was returned.");
            Log.i(TAG, "Caller can test if data is available by calling isDataAvailable()");
        }
    }


}
