package com.segfault.mytempo;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class PlayListActivity extends ActionBarActivity {

    Button myButton;
    Button[] buttArray;
    RelativeLayout.LayoutParams layoutParams;
    String URL;
    PopupMenu popup;
    RelativeLayout rLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list);
        URL = "http://24.124.68.225/playlists.php?pid=";
        new PlaylistTask().execute("all");
        rLayout = (RelativeLayout) findViewById(R.id.relLayout);

    }

    private void setOnClick(final Button btn,final PopupMenu popup, final JSONArray playlistJSON) {
        //popup.getMenuInflater()
        //        .inflate(R.menu.song_menu, popup.getMenu());

        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println(btn.getText());
                popup.show();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        int index = 0;
                        try {
                        for(int i = 0; i < playlistJSON.length(); i++)
                        {

                                JSONObject song = playlistJSON.getJSONObject(i);
                                System.out.println("EQUAL? " + song.getString("artist") + " - " + song.getString("title") + " == " + item.getTitle() + " " + ((song.getString("artist") + " - " + song.getString("title")).equals(item.getTitle())));
                                if(((song.getString("artist") + " - " + song.getString("title")).equals(item.getTitle().toString())))
                                {
                                    index = i;
                                    break;
                                }


                        }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Intent intent = new Intent(getApplicationContext(), MusicActivity.class);
                        intent.putExtra("song", index);
                        intent.putExtra("playlist",playlistJSON.toString());
                        startActivity(intent);
                        return true;
                    }
                });
            }
        });
    }

    private String getPlaylist(String param)
    {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(URL + param);
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

    private class PlaylistTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... param) {
            return getPlaylist(param[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            Log.e(MainActivity.class.toString(), "GOT IT!" + result);
            System.out.println(result);

            try {

                JSONObject obj = new JSONObject(result);
                JSONArray playlistJSON = obj.getJSONArray("playlists");
                buttArray = new Button[playlistJSON.length()];

                for (int i = 0; i < playlistJSON.length(); i++) {
                    makeButton(playlistJSON,i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void makeButton(JSONArray playlistJSON,int i)
    {
            myButton = new Button(this);
            myButton.setText(Integer.toString(i));
            popup = new PopupMenu(PlayListActivity.this, myButton);

            try{
                JSONObject jsonobject = playlistJSON.getJSONObject(i);
                System.out.println("Playlist: " + jsonobject.getString("name"));
                myButton.setText(jsonobject.getString("name"));
                //System.out.println(jsonobject.getString("songs"));
                JSONArray songJSONArray = jsonobject.getJSONArray("songs");

                for(int j = 0; j < songJSONArray.length(); j++) {
                    JSONObject songJSON = songJSONArray.getJSONObject(j);
                    System.out.println("Store id: " + songJSON.getString("id"));
                    //System.out.println(jsonobject.getString("storeId"));
                    //System.out.println("Title: " + songJSON.getString("title"));
                   // System.out.println("Artist: " + songJSON.getString("artist"));
                    popup.getMenu().add(songJSON.getString("artist") + " - " + songJSON.getString("title"));
                    System.out.println("MAKEBUTTON: TITLE: " + songJSON.getString("title") + " SONG INDEX: " + j);
                    setOnClick(myButton,popup,songJSONArray);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 150*i, 0, 200);
            myButton.setBackgroundColor(0xFFFFFFFF);
            rLayout.addView(myButton,layoutParams);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play_list, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
