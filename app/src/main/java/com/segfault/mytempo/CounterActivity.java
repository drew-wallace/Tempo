package com.segfault.mytempo;

import android.app.Activity;
import android.content.Context;
import android.hardware.*;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class CounterActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private TextView count,steps;
    boolean activityRunning;
    Timer timer = new Timer();
    float oldSteps=0,newSteps,oldTime,newTime;
    int check = 0;
    float stepsTaken = 0,stepsPerMin = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);
        oldTime = System.currentTimeMillis();
        count = (TextView) findViewById(R.id.count);
        steps = (TextView) findViewById(R.id.stepsPerMin);
        timeDelay();
        System.out.println("oncreate has run");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityRunning = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }

    }
    public CounterActivity()
    {
        /*oldTime = System.currentTimeMillis();
        activityRunning = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
            timeDelay();
        } else {
            stepsTaken = 1;
            stepsPerMin = 1;
            Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }*/
        System.out.println("counter");


    }
    @Override
    protected void onPause() {
        super.onPause();
        activityRunning = false;
        // if you unregister the last listener, the hardware will stop detecting step events
//        sensorManager.unregisterListener(this); 
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (activityRunning) {

            if(check == 0)
            {
                newSteps = event.values[0];
                stepsPerMin = (newSteps-oldSteps)*4;
                stepsTaken = newSteps - oldSteps;
                steps.setText(String.valueOf(stepsPerMin));
                count.setText(String.valueOf(stepsTaken));
                System.out.println("N-Steps " + newSteps + "\n");
                System.out.println("O-Steps " + oldSteps + "\n");
                oldSteps = newSteps;
                check = 1;
            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void timeDelay() {
        timer.schedule(new

                               TimerTask()
                               {
                                   public void run ()
                                   {
                                      check = 0;
                                      timeDelay();
                                   }
                               }
                ,15000);
    }
    public void onDestroy() {

        super.onDestroy();

        // Stop detecting
        sensorManager.unregisterListener(this);

        // Notifier
        Toast.makeText(this, "Tempo Stopped", Toast.LENGTH_SHORT).show();
    }
    public float returnSteps()
    {
        return stepsTaken;
    }
    public float returnStepsPerMin()
    {
        return stepsPerMin;
    }
}
