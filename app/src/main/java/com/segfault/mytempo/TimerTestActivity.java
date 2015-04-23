package com.segfault.mytempo;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Timer;
import java.util.TimerTask;

public class TimerTestActivity implements SensorEventListener {

    private boolean activityRunning;
    float oldSteps=0,newSteps = 0;
    private float stepsTaken = 0,stepsPerMin = 0;

    protected void onResume() {
        activityRunning = true;
    }
    public TimerTestActivity()
    {

    }
    public void testCounter(Sensor countSensor,Context THIS)
    {
        if (countSensor != null) {
            activityRunning = true;

        } else {
            activityRunning = false;
            Toast.makeText(THIS, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }
    }

    protected void onPause() {
        activityRunning = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (activityRunning) {
                stepsTaken = event.values[0];
        }

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    public float returnSteps()
    {
        return stepsTaken;
    }
    public float returnStepsPerMin()
    {
        return stepsPerMin;
    }
    public boolean countingSteps()
    {
        return activityRunning;
    }
}
