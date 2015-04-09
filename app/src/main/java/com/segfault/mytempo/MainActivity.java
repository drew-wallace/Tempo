package com.segfault.mytempo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    private NotificationManager mNM;
    //private PedometerSettings mPedometerSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Notifier
        Toast.makeText(this, "Tempo Started", Toast.LENGTH_SHORT).show();
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification();

        Button run = (Button) findViewById(R.id.Run);
        run.setOnClickListener (new View.OnClickListener()
        {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),CounterActivity.class);
                startActivity(i);
            }
        });
        Button nPlay = (Button) findViewById(R.id.Now_Playing);
        nPlay.setOnClickListener (new View.OnClickListener()
        {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),MusicActivity.class);
                startActivity(i);
            }
        });
        Button hist = (Button) findViewById(R.id.History);
        hist.setOnClickListener (new View.OnClickListener()
        {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),HistoryActivity.class);
                startActivity(i);
            }
        });
        Button loc = (Button) findViewById(R.id.My_Playlists);
        loc.setOnClickListener (new View.OnClickListener()
        {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),PlayListActivity.class);
                startActivity(i);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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



    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {/////icon
        CharSequence text = getText(R.string.app_name);
        Notification notification = new Notification(R.drawable.ic_notification, null,
                13);
        notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        Intent pedometerIntent = new Intent();
        //pedometerIntent.setComponent(new ComponentName(this, Pedometer.class));
        // pedometerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                pedometerIntent, 0);
        notification.setLatestEventInfo(this, text,
                "Counting Your Steps", contentIntent);

        mNM.notify(R.string.app_name, notification);
    }
    public void onDestroy() {

        super.onDestroy();

        // Stop detecting
        //mSensorManager.unregisterListener(mStepDetector);

        // Notifier
        Toast.makeText(this, "Tempo Stopped", Toast.LENGTH_SHORT).show();
        onClose();
    }
    public void cancelAll()
    {


    }
    public void onClose()
    {
        mNM.cancelAll();
        Toast.makeText(this,"Thanks for using application!!",Toast.LENGTH_LONG).show();
        finish();
        return;
    }

}