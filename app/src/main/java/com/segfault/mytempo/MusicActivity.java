package com.segfault.mytempo;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompatSideChannelService;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MusicActivity extends Activity  {

    private MediaPlayer mPlayer;
    Button buttonPlay, buttonStop, buttonSkip;
    ImageView imageCover;
    int duration = 100000;
    float oSteps = 0,nSteps = 0,spm = 0;
    Timer timer = new Timer();
    private String[][] songs;
    String url = "",pid,cluster;
    Intent intent;
    private JSONArray playlistJSON;
    int currentSong;
    boolean playListBool;
    URL urlpic;
    Bitmap image;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private TimerTestActivity stepCounter = null;

    ///Testing///
    private Run_HistoryActivity runHistory = null;
    private String stepsTaken;
    long start, end,timeWindow = 100000;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    public static final String TAG = "MusicApi";
    private static final int REQUEST_OAUTH = 1;
    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";
    private GoogleApiClient mClient = null;
    ///End///

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        intent = getIntent();

        Long test = System.currentTimeMillis();
        System.out.println("Test 1: " + test);
        test = test - 3600000;
        System.out.println("Test 2: " + test);

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        imageCover = (ImageView) findViewById(R.id.CoverArt);

        buttonPlay = (Button) findViewById(R.id.play);
        buttonPlay.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                setSong();
            }
        });

        buttonStop = (Button) findViewById(R.id.stop);
        buttonStop.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                end = System.currentTimeMillis();
                timeWindow = end - start;
                if(mPlayer!=null && mPlayer.isPlaying()){
                    mPlayer.stop();
                    mPlayer.reset();
                }
                runHistory = new Run_HistoryActivity(end,this);
                //buildFitnessClient();
            }
        });

        buttonSkip = (Button) findViewById(R.id.skip);
        buttonSkip.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                System.out.println("song skipped");
                skipSong();
            }
        });

        if(intent.hasExtra("song"))
        {
            playListBool = true;
            System.out.println("INTENT: " + intent.getExtras().getString("playlist"));
            try {
                currentSong = intent.getExtras().getInt("song");
                System.out.println("CURRENT SONG: " + currentSong);
                playlistJSON = new JSONArray(intent.getExtras().getString("playlist"));
                nextPlayListSong();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        else
        {
            System.out.println("NO INTENT");
            playListBool = false;
            songs = new String[5][4];
            //firstSong();
            buildFitnessClient();
        }
    }

    ////////////////five song selection//////////////
    public void firstSong()
    {
        new SeedTask().execute("http://24.124.68.225/5songs.php");
        setCounter();
    }

    private void genMenu()
    {
            //Creating the instance of PopupMenu
            Button anchorButton = (Button) findViewById(R.id.anchor);
            PopupMenu popup = new PopupMenu(MusicActivity.this, anchorButton);
            //Inflating the Popup using xml file
            popup.getMenuInflater()
                    .inflate(R.menu.popup_menu, popup.getMenu());

            popup.getMenu().findItem(R.id.one).setTitle(songs[0][0]);
            popup.getMenu().findItem(R.id.two).setTitle(songs[1][0]);
            popup.getMenu().findItem(R.id.three).setTitle(songs[2][0]);
            popup.getMenu().findItem(R.id.four).setTitle(songs[3][0]);
            popup.getMenu().findItem(R.id.five).setTitle(songs[4][0]);
            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {

                    if(item.getTitle().toString().compareTo("RESET") == 0)
                    {
                        firstSong();
                        //genSongBtn.setVisibility(View.INVISIBLE);
                    }
                    else {
                        String songTitle = item.getTitle().toString();
                        String songID = "";
                        if (songTitle.compareTo(songs[0][0]) == 0) {
                            songID = songs[0][1];
                            cluster = songs[0][3];
                        } else if (songTitle.compareTo(songs[1][0]) == 0) {
                            songID = songs[1][1];
                            cluster = songs[1][3];
                        } else if (songTitle.compareTo(songs[2][0]) == 0) {
                            songID = songs[2][1];
                            cluster = songs[2][3];
                        } else if (songTitle.compareTo(songs[3][0]) == 0) {
                            songID = songs[3][1];
                            cluster = songs[3][3];
                        } else if (songTitle.compareTo(songs[4][0]) == 0) {
                            songID = songs[4][1];
                            cluster = songs[4][3];
                        }
                        new FirstSongTask().execute(songID);

                        Toast.makeText(
                                MusicActivity.this,
                                "You Clicked : " + item.getTitle(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    return true;
                }
            });

            popup.show(); //showing popup menu
    }

    ///////SKIP BUTTON FUNCTION////////////
    public void skipSong()
    {
        mPlayer.reset();
        if(playListBool == false) {
            nextSong();
        }
        else
        {
            nextPlayListSong();
        }
    }
    ///////Plays next in playlist/////////
    public void nextPlayListSong()
    {
        try {
            JSONObject nextSong = playlistJSON.getJSONObject(currentSong);
            currentSong = (currentSong + 1) % playlistJSON.length();
            new setCoverArtTask().execute(nextSong.getString("albumArtRef"));
            new playListSongTask().execute(nextSong.getString("id"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
    /////////next song from generated songs/////
    public void nextSong()
    {
        System.out.println("http://24.124.68.225/nextSong.php?pid=" + pid + "&bpm=" + spm + "&cluster=" + cluster);
        new nextSongTask().execute("http://24.124.68.225/nextSong.php?pid=" + pid + "&bpm=" + spm + "&cluster=" + cluster);
    }

    //////Intializes counter/////////////
    public void setCounter()
    {
        stepCounter = new TimerTestActivity();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER );
        mSensorManager.registerListener(stepCounter,mSensor,SensorManager.SENSOR_DELAY_UI);
        stepCounter.testCounter(mSensor, this);
    }

    ///////sets song for media player////////////
    public void setSong()
    {
        try {
            mPlayer.setDataSource(url);
        } catch (IllegalArgumentException e) {
            //Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        } catch (SecurityException e) {
            //Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        } catch (IllegalStateException e) {
            //Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mPlayer.prepare();
            duration =  mPlayer.getDuration();
            playMusic();
        } catch (IllegalStateException e) {
            //Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            //Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        }
    }
    ///////starts media player/////////
    public void playMusic()
    {
        mPlayer.start();
        mPlayer.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mp.reset();
                if(playListBool == false) {
                    nextSong();
                }
                else
                {
                    nextPlayListSong();
                }
            }
        });
        if(stepCounter != null) {
            if (stepCounter.countingSteps() == true) {
                timeDelay();
            } else {
                spm = 140;
                System.out.println("SPM hard coded to 140");
            }
        }

    }

    protected void onDestroy() {
        super.onDestroy();
        // TODO Auto-generated method stub
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        // Connect to the Fitness API
        Log.i(TAG, "Connecting...");
        mClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mClient.isConnected()) {
            mClient.disconnect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mClient.isConnecting() && !mClient.isConnected()) {
                    mClient.connect();
                }
            }
        }
    }




    /////////TIMER FOR STEPS///////////////////////////
    public void timeDelay() {
        timer.schedule(new
           TimerTask()
           {
               public void run ()
               {
                   timeDelay();
               }
           }
        ,15000);
        stepCounter.setCheck(0);
        oSteps = nSteps;
        nSteps = stepCounter.returnSteps();
        spm = (nSteps - oSteps)*4;
        System.out.println("steps per min" + spm);
    }
    //////////SEEDER FOR FIRSTS SONG SELECTION//////////
    public String getSeeds(String address){
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(address);
        try{
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if(statusCode == 200){
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while((line = reader.readLine()) != null){
                    builder.append(line);
                }
            } else {
                Log.e(MainActivity.class.toString(), "Failedet JSON object");
            }
        }catch(ClientProtocolException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        System.out.println(builder.toString());
        return builder.toString();
    }

    private class SeedTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return getSeeds(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            //etResponse.setText(result);
            Log.e(MainActivity.class.toString(), "GOT IT!" + result);

            try {
                JSONObject obj = new JSONObject(result);
                JSONArray songJSON = obj.getJSONArray("choices");


                for (int i = 0; i < songJSON.length(); i++) {
                    JSONObject jsonobject = songJSON.getJSONObject(i);
                    songs[i][0] = jsonobject.getString("title");
                    songs[i][1] = jsonobject.getString("id");
                    songs[i][2] = jsonobject.getString("artist");
                    songs[i][3] = jsonobject.getString("cluster");
                    System.out.println(songs[i][0] + " " + songs[i][1] + " " + songs[i][2] + " " + i +  "\n");

                }
                //genSongBtn.setVisibility(View.VISIBLE);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            genMenu();
        }
    }
    ///////////////////////PLAY FIRST SONG///////////////////
    public String playFirstSong(String address, String sid){
        String fullURL = address.concat("?sid=").concat(sid);
        System.out.println(fullURL);
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(fullURL);
        try{
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if(statusCode == 200){
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while((line = reader.readLine()) != null){
                    builder.append(line);
                }
            } else {
                Log.e(MainActivity.class.toString(), "Failedet JSON object");
            }
        }catch(ClientProtocolException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        System.out.println(builder.toString());
        return builder.toString();
    }
    ///////////////NEXT SONGS////////////////////////
    public String getNextSong(String address){
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(address);
        try{
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if(statusCode == 200){
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while((line = reader.readLine()) != null){
                    builder.append(line);
                }
            } else {
                Log.e(MainActivity.class.toString(), "Failed JSON object");
            }
        }catch(ClientProtocolException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        System.out.println(builder.toString());
        return builder.toString();
    }
    private String getNextPlayListSong(String songID)
    {

        String fullURL = "http://24.124.68.225/streamURL.php?sid=".concat(songID).concat("&action=playlist");
        System.out.println(fullURL);
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(fullURL);
        try{
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if(statusCode == 200){
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while((line = reader.readLine()) != null){
                    builder.append(line);
                }
            } else {
                Log.e(MainActivity.class.toString(), "Failedet JSON object");
            }
        }catch(ClientProtocolException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        System.out.println(builder.toString());
        return builder.toString();
    }

    /////////////SONG TASKS/////////////////////////
    private class FirstSongTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... sid) {
            start = System.currentTimeMillis();
            return playFirstSong("http://24.124.68.225/streamURL.php", sid[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            parseUrl(result);

            //System.out.println(result);
            //Toast.makeText(getBaseContext(), "ReceivedNextSong", Toast.LENGTH_LONG).show();
        }
    }
    private class nextSongTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... urls)
        {
            return getNextSong(urls[0]);
        }

        @Override
        protected void onPostExecute(String result)
        {
            System.out.println("NEVER GONNA GIVE YOU UP " + result);
            parseNextSong(result);
        }
    }


    private class setCoverArtTask extends AsyncTask<String, Void, Bitmap>
    {
        //@Override
        protected Bitmap doInBackground(String... imageUrl)
        {
            try {
                urlpic = new URL(imageUrl[0]);
                image = BitmapFactory.decodeStream(urlpic.openConnection().getInputStream());
            }
            catch(MalformedURLException e)
            {

            } catch (IOException e) {
                e.printStackTrace();
            }

            return image;
        }

        @Override
        protected void onPostExecute(Bitmap result)
        {
            imageCover.setImageBitmap(result);
        }
    }

    private class playListSongTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... sid)
        {
            return getNextPlayListSong(sid[0]);
        }

        @Override
        protected void onPostExecute(String result)
        {
            parsePlayListUrl(result);
        }
    }


    ////////////////URL PARSERS////////////////////////////////////
    private void parseUrl(String result)
    {
        try {
            JSONObject obj = new JSONObject(result);
            url = obj.getString("streamURL");
            pid = obj.getString("pid");
            buttonPlay.setVisibility(View.VISIBLE);
            buttonStop.setVisibility(View.VISIBLE);
            buttonSkip.setVisibility(View.VISIBLE);
            setSong();
            }
         catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseNextSong(String result)
    {
        try {
            JSONObject obj = new JSONObject(result);
            url = obj.getString("streamURL");
            setSong();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parsePlayListUrl(String result)
    {
        try {
            JSONObject obj = new JSONObject(result);
            url = obj.getString("streamURL");
            buttonPlay.setVisibility(View.VISIBLE);
            buttonStop.setVisibility(View.VISIBLE);
            buttonSkip.setVisibility(View.VISIBLE);
            setSong();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //////////////////TESTING////////////////////////////////////
    private void buildFitnessClient() {
        // Create the Google API Client
        System.out.println("building stuff");
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i(TAG, "Connected!!!");
                                // Now you can make calls to the Fitness APIs.  What to do?
                                // Look at some data!!
                                new InsertAndVerifyDataTask().execute();
                                //printTxt();
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
                                }
                            }
                        }
                )
                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            // Called whenever the API client fails to connect.
                            @Override
                            public void onConnectionFailed(ConnectionResult result) {
                                Log.i(TAG, "Connection failed. Cause: " + result.toString());
                                if (!result.hasResolution()) {
                                    // Show the localized error dialog
                                    GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                                            MusicActivity.this, 0).show();
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                if (!authInProgress) {
                                    try {
                                        Log.i(TAG, "Attempting to resolve failed connection");
                                        authInProgress = true;
                                        result.startResolutionForResult(MusicActivity.this,
                                                REQUEST_OAUTH);
                                    } catch (IntentSender.SendIntentException e) {
                                        Log.e(TAG,
                                                "Exception while starting resolution activity", e);
                                    }
                                }
                            }
                        }
                )
                .build();
    }
    private class InsertAndVerifyDataTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... result) {
            DataReadRequest readRequest = queryFitnessData();
            DataReadResult dataReadResult =
                    Fitness.HistoryApi.readData(mClient, readRequest).await(1, TimeUnit.MINUTES);
            printData(dataReadResult);
            return result[0];
        }
        protected void onPostExecute(String result) {
            System.out.println("Steps taken: " + stepsTaken);
        }
    }

    /**
     * Create and return a {@link com.google.android.gms.fitness.data.DataSet} of step count data for the History API.
     */
    private DataReadRequest queryFitnessData() {
        // [START build_read_data_request]
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.MINUTE, -Math.round(timeWindow));
        long startTime = cal.getTimeInMillis();

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
        Log.i(TAG, "Range End: " + dateFormat.format(endTime));

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(Math.round(timeWindow), TimeUnit.MINUTES)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        return readRequest;
    }
    private void printData(DataReadResult dataReadResult) {
        if (dataReadResult.getBuckets().size() > 0) {
            Log.i(TAG, "Number of returned buckets of DataSets is: "
                    + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.i(TAG, "Number of returned DataSets is: "
                    + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }
    }

    private void dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
                stepsTaken = "" + dp.getValue(field);
                System.out.println("Steps taken: " + stepsTaken);
            }
        }
    }
}