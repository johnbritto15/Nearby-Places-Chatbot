package com.example.hmv.chatbot;

import android.database.DataSetObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIContext;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.JsonElement;
import ai.api.model.AIRequest;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private Button listenButton;
    private TextView resultTextView;
    private EditText textV;
    private AIService aiService;
    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatText;
    private Button buttonSend;
    private boolean side = false;
    private boolean text_to_speech=true;
    private Menu menu;
    private TrackGPS gps;
    double longitude,latitude;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonSend = (Button) findViewById(R.id.send);

        listView = (ListView) findViewById(R.id.msgview);
//
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);


        /*listenButton = (Button) findViewById(R.id.send);
        textV = (EditText) findViewById(R.id.editText);
        final TextView resultTextView1 = (TextView) findViewById(R.id.textView);
        resultTextView = (TextView) findViewById(R.id.textView1);*/
        tts = new TextToSpeech(this, this);
        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.right);
        listView.setAdapter(chatArrayAdapter);


        chatText = (EditText) findViewById(R.id.msg);
        chatText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage(1);
                }
                return false;
            }
        });

//        buttonSend.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                sendChatMessage();
//            }
//        });

        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(chatArrayAdapter);

        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });

        final AIConfiguration config = new AIConfiguration("a540f456608a459db3de8fd6458b9759",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        final AIDataService aiDataService = new AIDataService(config);

        final AIRequest aiRequest = new AIRequest();
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        buttonSend.setOnClickListener(new View.OnClickListener(){

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                String input =chatText.getText().toString().trim();
                if(!input.equals(""))
                {
                    aiRequest.setQuery(input);

                    sendChatMessage(1);
                    //aiService.setListener(this);
                    new AsyncTask<AIRequest, Void, AIResponse>() {
                        @Override
                        protected AIResponse doInBackground(AIRequest... requests) {
                            final AIRequest request = requests[0];
                            try {
                                final AIResponse response = aiDataService.request(aiRequest);
                                return response;
                            } catch (AIServiceException e) {
                            }
                            return null;
                        }

                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        protected void onPostExecute(AIResponse aiResponse) {
                            if (aiResponse != null) {
                                Result result = aiResponse.getResult();

                                // Get parameters
                                String parameterString = "";
                                String resultString = "";

                                if (result.getParameters() != null && !result.getParameters().isEmpty()) {
                                    for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                                        parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
                                    }

                                }
                                if (result.getFulfillment() != null) {
                                    resultString += result.getFulfillment().getSpeech() ;
                                }
                                // Show results in TextView.
                                chatText.setText(resultString);

                                if(text_to_speech==true)
                                {
                                    speakOut();

                                }
//                                sendChatMessage();
                                String action=result.getAction();
                                if(action.compareTo("places")==0&&result.getParameters().get("geo-city")!=null&&result.getParameters().get("place-attraction").toString()!=null)
                                {
                                    sendChatMessage(2);
                                    urlCall(result.getParameters().get("geo-city").toString(),result.getParameters().get("place-attraction").toString());
                                }
                                else
                                {
                                    sendChatMessage(2);
                                }



                            }
                        }
                    }.execute(aiRequest);
                }
                }

        });

    }
    public boolean sendChatMessage(int i) {
        if(i==1)
            side = false;
        else
            side = true;
        chatArrayAdapter.add(new ChatMessage(side, chatText.getText().toString()));
        chatText.setText("");

        return true;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                buttonSend.setEnabled(true);
                speakOut();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void speakOut() {

        String text = chatText.getText().toString();

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null,null);
    }
    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
        gps.stopUsingGPS();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        this.menu = menu;

        return true;
    }

    public void onTextToSpeech(MenuItem menuItem)
    {
        if(text_to_speech==true)
        {
            Toast.makeText(this, "Text to speech is off", Toast.LENGTH_SHORT).show();
            menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.tts_off));

        }
        else if(text_to_speech==false)
        {
            Toast.makeText(this, "Text to speech is On", Toast.LENGTH_SHORT).show();
            menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.tts_on));
        }

        text_to_speech=!text_to_speech;



    }
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

                        JsonExtractor je=new JsonExtractor(response);
                        String fResponse=je.reqResponse();
                        chatText.setText(fResponse);

                        sendChatMessage();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                chatText.setText("That didn't work!");
                sendChatMessage(2);
            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void location()
    {
        gps = new TrackGPS(MainActivity.this);
        if(gps.canGetLocation()){
            longitude = gps.getLongitude();
            latitude = gps .getLatitude();

        }
        else
        {
            gps.showSettingsAlert();
        }

    }

}
//    public void onResult(final AIResponse response) {
//        Result result = response.getResult();
//
//        // Get parameters
//        String parameterString = "";
//        String resultString = "";
//
//        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
//            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
//                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
//
//
//            }
//
//        }
//        if (result.getFulfillment() != null) {
//            resultString += "(" + result.getFulfillment().getSpeech() + ") ";
//        }
//        // Show results in TextView.
//        resultTextView.setText("Query:" + result.getResolvedQuery() +
//                "\nAction: " + result.getAction() +
//                "\nParameters: " + parameterString +
//                "\nReply:" + resultString);
//    }
//
//    @Override
//    public void onError(final AIError error) {
//        resultTextView.setText(error.toString());
//    }
//
//    @Override
//    public void onListeningStarted() {
//    }
//
//    @Override
//    public void onListeningCanceled() {
//    }
//
//    @Override
//    public void onListeningFinished() {
//    }
//
//    @Override
//    public void onAudioLevel(final float level) {
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client.connect();
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "Main Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app URL is correct.
//                Uri.parse("android-app://com.example.zainahmeds.chatbot/http/host/path")
//        );
//        AppIndex.AppIndexApi.start(client, viewAction);
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "Main Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app URL is correct.
//                Uri.parse("android-app://com.example.zainahmeds.chatbot/http/host/path")
//        );
//        AppIndex.AppIndexApi.end(client, viewAction);
//        client.disconnect();
//    }
//}
