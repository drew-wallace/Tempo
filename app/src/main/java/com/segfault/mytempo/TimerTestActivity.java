package com.segfault.mytempo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.Toast;

public class TimerTestActivity implements SensorEventListener {

    private boolean activityRunning;
    float oldSteps=0,newSteps = 0;
    private float stepsTaken = 0,stepsPerMin = 0;
    private float initial = 0;

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
        if(initial == 0){
            initial = event.values[0];
        }
        if (activityRunning) {
                stepsTaken = event.values[0] - initial;
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
