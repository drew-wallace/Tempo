package com.segfault.mytempo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    NotificationManager notificationManager;
    Notification myNotification;
    private static final int MY_NOTIFICATION_ID=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Notifier
        Toast.makeText(this, "Tempo Started", Toast.LENGTH_SHORT).show();
        showNotification();

        Button run = (Button) findViewById(R.id.stop);
        run.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), CounterActivity.class);
                startActivity(i);
            }
        });
        Button nPlay = (Button) findViewById(R.id.CoverArt);
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
        Button about = (Button) findViewById(R.id.About);
        about.setOnClickListener (new View.OnClickListener()
        {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),AboutActivity.class);
                startActivity(i);
            }
        });
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void showNotification() {

        Context context = getApplicationContext();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                MainActivity.this,
                0,
                this.getIntent(),
                0);

        myNotification = new Notification.Builder(context)
                .setContentTitle("MyTempo!")
                .setContentText("Playing at the Speed at You")
                .setTicker("RunningWithYou!")
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_SOUND)
                //.setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification)
                .build();

        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(MY_NOTIFICATION_ID, myNotification);
    }
    public void onDestroy() {

        super.onDestroy();
        notificationManager.cancelAll();
        Toast.makeText(this, "We're always watching", Toast.LENGTH_SHORT).show();
    }

    public void onClose()
    {
        notificationManager.cancelAll();
        Toast.makeText(this,"We're always watching",Toast.LENGTH_LONG).show();
        finish();
        return;
    }

}