package com.segfault.mytempo;

import android.app.Activity;
import android.content.Context;
import android.hardware.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class CounterActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private TextView count;
    boolean activityRunning;
    Timer timer = new Timer();
    float stepsTaken = 0,stepsPerMin = 0,initial;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);

        count = (TextView) findViewById(R.id.count);
        stepsTaken = 0;
        count.setText("" + stepsTaken);
        System.out.println("oncreate has run");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        startTimer();
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
        System.out.println("counter");
    }
    @Override
    protected void onPause() {
        super.onPause();
        activityRunning = false;
        // if you unregister the last listener, the hardware will stop detecting step events
       sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (activityRunning) {

            if(initial == 0){
                initial = event.values[0];
            }
            stepsTaken = event.values[0] - initial;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void startTimer() {
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                mHandler.obtainMessage(1).sendToTarget();

            }
        }, 0, 1000);
    };

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            count.setText("" + stepsTaken);
        }
    };
    public void onDestroy() {

        super.onDestroy();

        // Stop detecting
        sensorManager.unregisterListener(this);
        timer.cancel();

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
