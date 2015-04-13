package com.segfault.mytempo;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompatSideChannelService;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Timer;
import java.util.TimerTask;

public class MusicActivity extends Activity  {

    private MediaPlayer mPlayer;
    Button buttonPlay;
    Button buttonStop;
    Button buttonSkip;
    private TextView dur;
    int duration = 100000;
    float oSteps = 0,nSteps = 0,spm = 0;
    private Button genSongBtn;
    Timer timer = new Timer();
    private String[][] songs;
    String url = "",pid,cluster;
    Intent intent;
    private JSONArray playlistJSON;
    int currentSong;
    boolean playListBool;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private TimerTestActivity stepCounter = null;
    private Run_HistoryActivity run_history = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        intent = getIntent();
        Long test = System.currentTimeMillis() - 3600000;
        run_history = new Run_HistoryActivity(test,this);

        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        buttonPlay = (Button) findViewById(R.id.play);
        buttonPlay.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                setSong();
            }
        });
        buttonStop = (Button) findViewById(R.id.stop);
        buttonStop.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(mPlayer!=null && mPlayer.isPlaying()){
                    mPlayer.stop();
                }
            }
        });
        buttonSkip = (Button) findViewById(R.id.skip);
        buttonSkip.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                System.out.println("song skipped");

                skipSong();
            }
        });
        genSongBtn = (Button) findViewById(R.id.button1);

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
            firstSong();
        }
    }

    ////////////////five song selection//////////////
    public void firstSong()
    {

        genSongBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("=====" + run_history.returnSteps());
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(MusicActivity.this, genSongBtn);
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
                            genSongBtn.setVisibility(View.INVISIBLE);
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
        }); //closing the setOnClickListener method

        new SeedTask().execute("http://24.124.68.225/5songs.php");
        setCounter();
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
            System.out.println("DAT HOLE PLAYLIST: " + playlistJSON.toString(3));
            JSONObject nextSong = playlistJSON.getJSONObject(currentSong);
            currentSong = (currentSong + 1) % playlistJSON.length();
            System.out.println("THA NEXT SONG TAH BE PLAID: " + nextSong.getString("title"));
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
        //new nextSongTask().execute("http://24.124.68.225/nextSong.php?pid=3.1459&bpm=142.9&cluster=42");
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
                System.out.println("song done");
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
                genSongBtn.setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                e.printStackTrace();
            }

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
            parseNextSong(result);
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
            System.out.println("DIS IS DAT RESULT. HOPE IT HALPS: " + result);
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
            genSongBtn.setVisibility(View.INVISIBLE);
            //System.out.println(stepCounter.returnSteps() + " " + stepCounter.returnStepsPerMin());
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
            //System.out.println(stepCounter.returnSteps() + " " + stepCounter.returnStepsPerMin());
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
            genSongBtn.setVisibility(View.INVISIBLE);
            //System.out.println(stepCounter.returnSteps() + " " + stepCounter.returnStepsPerMin());
            setSong();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}