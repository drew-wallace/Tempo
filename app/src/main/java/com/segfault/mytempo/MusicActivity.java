package com.segfault.mytempo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class MusicActivity extends ActionBarActivity {

    private MediaPlayer mPlayer,mPlayerTmp;
    Button buttonPlay, buttonStop, buttonSkip;
    ImageView imageCover;
    int playing = 1,secondsD,minutesD,songDur,check;
    float oSteps = 0, nSteps = 0, spm = 0;
    Timer timer = new Timer();
    private String[][] songs;
    String url = "", pid, cluster, artCover;
    Intent intent;
    private JSONArray playlistJSON;
    int currentSong,countingSteps = -1;
    boolean playListBool;
    URL urlpic;
    Bitmap image;
    TextView songTV,dur;
    String songName;
    SeekBar progress;
    boolean nextClicked = false;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private TimerTestActivity stepCounter = null;
    private MediaObserver observer = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        intent = getIntent();

        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayerTmp = new MediaPlayer();
        mPlayerTmp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        progress = (SeekBar) findViewById(R.id.progressBar);

        imageCover = (ImageView) findViewById(R.id.CoverArt);
        songTV = (TextView) findViewById(R.id.songTextView);
        dur = (TextView) findViewById(R.id.dur);
        check = -1;

        buttonPlay = (Button) findViewById(R.id.play);
        buttonPlay.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (playing == 0) {
                    playing = 1;
                    mPlayer.start();
                    buttonPlay.setBackgroundResource(android.R.drawable.ic_media_pause);

                } else {
                    playing = 0;
                    mPlayer.pause();
                    buttonPlay.setBackgroundResource(android.R.drawable.ic_media_play);
                }
            }
        });

        buttonStop = (Button) findViewById(R.id.stop);
        buttonStop.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                onBackPressed();
            }
        });

        buttonSkip = (Button) findViewById(R.id.skip);
        buttonSkip.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                System.out.println("song skipped");
                observer.stop();
                if (playing == 0) {
                    playing = 1;
                    buttonPlay.setBackgroundResource(android.R.drawable.ic_media_pause);

                }
                skipSong();
            }
        });

        if (intent.hasExtra("song")) {
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

        } else {
            System.out.println("NO INTENT");
            playListBool = false;
            songs = new String[5][5];
            firstSong();
        }
    }

    ////////////////five song selection//////////////
    public void firstSong() {
        //new getSongsJSOUPTask().execute();
        new SeedTask().execute("http://24.124.68.225/5songs.php");
        setCounter();
    }

    private void genMenu() {
        //Creating the instance of PopupMenu
        Button anchorButton = (Button) findViewById(R.id.anchor);
        PopupMenu popup = new PopupMenu(MusicActivity.this, anchorButton);
        //Inflating the Popup using xml file
        popup.getMenuInflater()
                .inflate(R.menu.popup_menu, popup.getMenu());

        popup.getMenu().findItem(R.id.one).setTitle("Song: " + songs[0][0]);
        popup.getMenu().findItem(R.id.oneName).setTitle("Artist: " + songs[0][2]);
        popup.getMenu().findItem(R.id.oneName).setEnabled(false);
        popup.getMenu().findItem(R.id.two).setTitle("Song: " + songs[1][0]);
        popup.getMenu().findItem(R.id.twoName).setTitle("Artist: " + songs[1][2]);
        popup.getMenu().findItem(R.id.twoName).setEnabled(false);
        popup.getMenu().findItem(R.id.three).setTitle("Song: " + songs[2][0]);
        popup.getMenu().findItem(R.id.threeName).setTitle("Artist: " + songs[2][2]);
        popup.getMenu().findItem(R.id.threeName).setEnabled(false);
        popup.getMenu().findItem(R.id.four).setTitle("Song: " + songs[3][0]);
        popup.getMenu().findItem(R.id.fourName).setTitle("Artist: " + songs[3][2]);
        popup.getMenu().findItem(R.id.fourName).setEnabled(false);
        popup.getMenu().findItem(R.id.five).setTitle("Song: " + songs[4][0]);
        popup.getMenu().findItem(R.id.fiveName).setTitle("Artist: " + songs[4][2]);
        popup.getMenu().findItem(R.id.fiveName).setEnabled(false);
        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getTitle().toString().compareTo("RESET") == 0) {
                    firstSong();
                } else {
                    String songTitle = item.getTitle().toString();
                    songTitle = songTitle.substring(songTitle.indexOf(':') + 2,songTitle.length());
                    System.out.println(songTitle);
                    String songID = "";
                    if (songTitle.compareTo(songs[0][0]) == 0) {
                        songName = songs[0][0] + " - " + songs[0][2];
                        songID = songs[0][1];
                        cluster = songs[0][3];
                        artCover = songs[0][4];
                    } else if (songTitle.compareTo(songs[1][0]) == 0) {
                        songName = songs[1][0] + " - " + songs[1][2];
                        songID = songs[1][1];
                        cluster = songs[1][3];
                        artCover = songs[1][4];
                    } else if (songTitle.compareTo(songs[2][0]) == 0) {
                        songName = songs[2][0] + " - " + songs[2][2];
                        songID = songs[2][1];
                        cluster = songs[2][3];
                        artCover = songs[2][4];
                    } else if (songTitle.compareTo(songs[3][0]) == 0) {
                        songName = songs[3][0] + " - " + songs[3][2];
                        songID = songs[3][1];
                        cluster = songs[3][3];
                        artCover = songs[3][4];
                    } else if (songTitle.compareTo(songs[4][0]) == 0) {
                        songName = songs[4][0] + " - " + songs[4][2];
                        songID = songs[4][1];
                        cluster = songs[4][3];
                        artCover = songs[4][4];
                    }
                    //new setCoverArtTask().execute(artCover);
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
    public void skipSong() {
        mPlayer.reset();
        if (playListBool == false) {
            nextSong();
        } else {
            nextPlayListSong();
        }
    }

    ///////Plays next in playlist/////////
    public void nextPlayListSong() {
        try {
            JSONObject nextSong = playlistJSON.getJSONObject(currentSong);
            currentSong = (currentSong + 1) % playlistJSON.length();
            new setCoverArtTask().execute(nextSong.getString("albumArtRef"));
            new playListSongTask().execute(nextSong.getString("id"), nextSong.getString("title"), nextSong.getString("artist"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /////////next song from generated songs/////
    public void nextSong() {
        System.out.println("http://24.124.68.225/nextSong.php?pid=" + pid + "&bpm=" + spm + "&cluster=" + cluster);
        new nextSongTask().execute("http://24.124.68.225/nextSong.php?pid=" + pid + "&bpm=" + spm + "&cluster=" + cluster);
    }

    //////Intializes counter/////////////
    public void setCounter() {
        stepCounter = new TimerTestActivity();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.registerListener(stepCounter, mSensor, SensorManager.SENSOR_DELAY_UI);
        stepCounter.testCounter(mSensor, this);
        if (stepCounter != null) {
            if (stepCounter.countingSteps() == true) {
                timeDelay();
                countingSteps = 1;
            } else {
                openOptionsMenu();
                //System.out.println("SPM hard coded to 140");
            }
        }
    }

    ///////sets song for media player////////////
    public void setSong() {
        try {
            if(check == -1) {
                mPlayer.setDataSource(url);
            }
           else
            {
                mPlayerTmp.setDataSource(url);
            }
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

            if(check == -1) {
                check = 0;
                mPlayer.prepare();
                playMusic();
            }
            else
            {

                mPlayerTmp.prepare();
            }
        } catch (IllegalStateException e) {
            //Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            //Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        }
    }

    ///////starts media player/////////
    public void playMusic() {
        playing = 1;
        check = 0;
        mPlayer.start();
        new setCoverArtTask().execute(artCover);
        songDur = mPlayer.getDuration();
        observer = new MediaObserver();
        new Thread(observer).start();
        progress.setMax(mPlayer.getDuration());
        secondsD = mPlayer.getDuration()/1000;
        minutesD = (secondsD % 3600)/60;
        secondsD = secondsD % 60;

        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 0;


            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                progressChanged = progress;
                int seconds = progress/1000;
                int minutes = (seconds % 3600) / 60;
                seconds = seconds % 60;

                dur.setText(minutes + ":" + (seconds < 10 ? "0" : "") + seconds + "/" + minutesD + ":" + (secondsD < 10 ? "0" : "") + secondsD);
                if(progress >= songDur-15000 && check == 0)
                {
                    check = 1;
                    if (playListBool == false) {
                        nextSong();
                    } else {
                        nextPlayListSong();
                    }
                }
                if(fromUser) {
                    mPlayer.seekTo(progressChanged);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(SeekbarActivity.this,"seek bar progress:"+progressChanged,
                //Toast.LENGTH_SHORT).show();
            }
        });

        mPlayer.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mp.reset();
                observer.stop();
                check = 0;
                //mPlayer = mPlayerTmp.clone();

                playMusic();
                mPlayerTmp.reset();
                /*if (playListBool == false) {
                    nextSong();
                } else {
                    nextPlayListSong();
                }*/
            }
        });
    }

    protected void onDestroy() {
        super.onDestroy();
        //timer.cancel();
        // TODO Auto-generated method stub
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
            //observer.stop();
        }
        if (playListBool == false) {
            timer.cancel();

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (playListBool == false) {
            timer.cancel();


        }
    }

    /////////TIMER FOR STEPS///////////////////////////
    public void timeDelay() {
        timer.schedule(new
                               TimerTask() {
                                   public void run() {
                                       timeDelay();
                                   }
                               }
                , 15000);
        nSteps = stepCounter.returnSteps();
        spm = (nSteps - oSteps) * 4;
        oSteps = nSteps;
        System.out.println("steps per min " + spm);
    }

    //////////SEEDER FOR FIRSTS SONG SELECTION//////////
    public String getSeeds(String address) {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(address);
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                Log.e(MainActivity.class.toString(), "Failedet JSON object");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
                    songs[i][4] = jsonobject.getString("albumArtRef");
                    System.out.println(songs[i][0] + " " + songs[i][1] + " " + songs[i][2] + " " + songs[i][3] + " " + songs[i][4] + i + "\n");

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            genMenu();
        }
    }

    ///////////////////////PLAY FIRST SONG///////////////////
    public String playFirstSong(String address, String sid) {
        String fullURL = address.concat("?sid=").concat(sid);
        System.out.println(fullURL);
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(fullURL);
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                Log.e(MainActivity.class.toString(), "Failedet JSON object");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(builder.toString());
        return builder.toString();
    }

    ///////////////NEXT SONGS////////////////////////
    public String getNextSong(String address) {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(address);
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                Log.e(MainActivity.class.toString(), "Failed JSON object");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(builder.toString());
        return builder.toString();
    }

    private String getNextPlayListSong(String songID) {

        String fullURL = "http://24.124.68.225/streamURL.php?sid=".concat(songID).concat("&action=playlist");
        System.out.println(fullURL);
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(fullURL);
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                Log.e(MainActivity.class.toString(), "Failedet JSON object");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
        }
    }

    private class nextSongTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            System.out.println("TIS IS ARE LYNC: " + urls[0]);
            return getNextSong(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println("NEVER GONNA GIVE YOU UP " + result);
            parseNextSong(result);
        }
    }


    private class setCoverArtTask extends AsyncTask<String, Void, Bitmap> {
        //@Override
        protected Bitmap doInBackground(String... imageUrl) {
            try {
                if (imageUrl[0].compareTo("none") != 0) {
                    urlpic = new URL(imageUrl[0]);
                    image = BitmapFactory.decodeStream(urlpic.openConnection().getInputStream());
                } else {
                    image = BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification);
                }
            } catch (MalformedURLException e) {

            } catch (IOException e) {
                e.printStackTrace();
            }

            return image;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            imageCover.setImageBitmap(result);
            songTV.setText("PLAYING: " + songName);
            songTV.setHorizontallyScrolling(true);
        }
    }

    private class playListSongTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... sid) {
            songName = sid[1] + " - " + sid[2];
            return getNextPlayListSong(sid[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            parsePlayListUrl(result);
        }
    }


    ////////////////URL PARSERS////////////////////////////////////
    private void parseUrl(String result) {
        try {
            JSONObject obj = new JSONObject(result);
            url = obj.getString("streamURL");
            pid = obj.getString("pid");
            //buttonPlay.setVisibility(View.VISIBLE);
            //buttonStop.setVisibility(View.VISIBLE);
            //buttonSkip.setVisibility(View.VISIBLE);
            setSong();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseNextSong(String result) {
        try {

            JSONObject obj = new JSONObject(result);
            JSONObject song = new JSONObject(obj.getString("song"));
            artCover = song.getString("albumArtRef");
            url = obj.getString("streamURL");
            songName = song.getString("title") + " " + song.getString("artist");

            //new setCoverArtTask().execute(artCover);
            setSong();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parsePlayListUrl(String result) {
        try {
            JSONObject obj = new JSONObject(result);
            url = obj.getString("streamURL");
            System.out.println(obj);
            //buttonPlay.setVisibility(View.VISIBLE);
            //buttonStop.setVisibility(View.VISIBLE);
            //buttonSkip.setVisibility(View.VISIBLE);
            setSong();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class MediaObserver implements Runnable {
        private AtomicBoolean stop = new AtomicBoolean(false);

        public void stop() {
            stop.set(true);
        }

        @Override
        public void run() {
            while (!stop.get() && mPlayer != null) {
                progress.setProgress(mPlayer.getCurrentPosition());

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_music, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.one:
                Toast.makeText(getApplicationContext(), "120 spm Selected", Toast.LENGTH_LONG).show();
                spm = 120;
                if(countingSteps == 1)
                {
                    timer.cancel();
                }
                countingSteps = 0;
                return true;
            case R.id.two:
                Toast.makeText(getApplicationContext(), "140 spm Selected", Toast.LENGTH_LONG).show();
                spm = 140;

                if(countingSteps == 1)
                {
                    timer.cancel();
                }
                countingSteps = 0;
                return true;
            case R.id.three:
                Toast.makeText(getApplicationContext(), "160 spm Selected", Toast.LENGTH_LONG).show();
                spm = 160;

                if(countingSteps == 1)
                {
                    timer.cancel();
                }
                countingSteps = 0;
                return true;
            case R.id.four:
                Toast.makeText(getApplicationContext(), "180 spm Selected", Toast.LENGTH_LONG).show();
                spm = 180;
                if(countingSteps == 1)
                {
                    timer.cancel();
                }
                countingSteps = 0;
                return true;
            case R.id.five:
                Toast.makeText(getApplicationContext(), "Stop Selected", Toast.LENGTH_LONG).show();
                spm = 0;
                if(countingSteps == 1)
                {
                    timer.cancel();
                }
                countingSteps = 0;
                return true;
            case R.id.six:
                Toast.makeText(getApplicationContext(),"Counting Seleted", Toast.LENGTH_LONG).show();
                if(countingSteps == 0)
                {
                    timer = new Timer();
                    setCounter();
                }
            case R.id.seven:
                Toast.makeText(getApplicationContext(), "Close Selected", Toast.LENGTH_LONG).show();
                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
