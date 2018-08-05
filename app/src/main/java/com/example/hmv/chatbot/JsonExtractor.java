package com.example.hmv.chatbot;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.ContentValues.TAG;

/**
 * Created by zainahmeds on 7/4/17.
 */

public class JsonExtractor {
    String jsonResponse;
    JsonExtractor(String jsonResponse)
    {
        this.jsonResponse=jsonResponse;
    }
    public String reqResponse()
    {
        String resp="";
        if(jsonResponse!=null) {
            try {
                JSONObject places = new JSONObject(jsonResponse);
                JSONArray results = places.getJSONArray("results");
                for (int i = 0; i < results.length() && i < 4; i++) {

                    JSONObject placeone = results.getJSONObject(i);
                    String name = placeone.getString("name");
                    String formatted_address = placeone.getString("formatted_address");
                    JSONObject openingHours=placeone.getJSONObject("opening_hours");
                    boolean open_now = openingHours.getBoolean("open_now");
                    String openNowYesNo = open_now ? "This Place is Open Currently" : "This Place is Closed at this moment";
                    resp += name + "\n" + formatted_address  + "\n" +openNowYesNo+ "\n\n\n";
                }
                return resp;
            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
                return "Sorry Some Error Occured Please Try Again! "+e.getMessage();
            }
        }
        else
        {
            return "Empty";
        }
    }
}
