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
    private int check = 0;
    private float stepsTaken = 0,stepsPerMin = 0;

    protected void onResume() {
        activityRunning = true;
        /*Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }
        */
    }
    public TimerTestActivity()
    {

    }
    public void testCounter(Sensor countSensor,Context THIS)
    {
        if (countSensor != null) {
            activityRunning = true;
            //sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);

        } else {
            activityRunning = false;
            Toast.makeText(THIS, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }
    }

    protected void onPause() {
        activityRunning = false;
        // if you unregister the last listener, the hardware will stop detecting step events
//        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (activityRunning) {

            if(check == 0)
            {
                newSteps = event.values[0];
                stepsPerMin = (newSteps-oldSteps)*4;
                stepsTaken = newSteps - oldSteps;
                //steps.setText(String.valueOf(stepsPerMin));
                //count.setText(String.valueOf(stepsTaken));
                System.out.println("N-Steps " + newSteps + "\n");
                System.out.println("O-Steps " + oldSteps + "\n");
                oldSteps = newSteps;
                check = 1;
            }

        }

    }
    public void setCheck(int x)
    {
        check = x;
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
