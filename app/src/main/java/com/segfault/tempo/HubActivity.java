package com.segfault.tempo;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class HubActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hub);

        Button run = (Button) findViewById(R.id.Run);
        run.setOnClickListener (new View.OnClickListener()
        {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),RunActivity.class);
                startActivity(i);
            }
        });
        Button nowPlaying = (Button) findViewById(R.id.Now_Playing);
        nowPlaying.setOnClickListener (new View.OnClickListener()
        {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),Now_PlayingActivity.class);
                startActivity(i);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hub, menu);
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
