package com.example.hmv.chatbot;

import android.content.Context;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by zainahmeds on 6/4/17.
 */

public class GooglePlacesUrl extends MainActivity{
    private EditText chatText;

    MainActivity mainObj=new MainActivity();

    public void urlCall(String city,String place){
// Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://maps.googleapis.com/maps/api/place/textsearch/json?query="+place+"+in+"+city+"&key=AIzaSyAV-JsxOAqem1U1sSBchQjCQqDaY0qJDfk";

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.

                        chatText.setText("Response is: "+ response);
                        mainObj.sendChatMessage(1);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                chatText.setText("That didn't work!");
                mainObj.sendChatMessage(1);
            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
