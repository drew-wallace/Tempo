package com.segfault.mytempo;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import org.w3c.dom.Text;

import java.io.FileOutputStream;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import java.util.logging.LogRecord;


public class Run_HistoryActivity extends ActionBarActivity {

    public static final String TAG = "RunHistoryApi";
    private static final int REQUEST_OAUTH = 1;
    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";
    private String tmp;
    private float timeWindow;
    FileOutputStream outputStream;
    Context context;

    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;

    private GoogleApiClient mClient = null;


    Run_HistoryActivity(long start,Context contextTmp)
    {
        System.out.println("-----" + start);
        long end = System.currentTimeMillis();
        timeWindow = end - start;
        this.context = contextTmp;
        System.out.println("-----" + timeWindow);
        Log.i(TAG, "Connecting...");
        buildFitnessClient();
        mClient.connect();
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run__history);

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        buildFitnessClient();
    }

    public String returnSteps()
    {
        return tmp;
    }
    private void buildFitnessClient() {
        // Create the Google API Client
        System.out.println("build fitness client");
        mClient = new GoogleApiClient.Builder(this.context)
                .addApi(Fitness.API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i(TAG, "Connected!!!");
                                System.out.println("Connected");
                                new InsertAndVerifyDataTask().execute();
                            }
                            @Override
                            public void onConnectionSuspended(int i) {
                                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                    System.out.println("Connected 2");
                                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
                                    System.out.println("Connected 3");
                                }
                            }
                        }
                )
                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            // Called whenever the API client fails to connect.
                            @Override
                            public void onConnectionFailed(ConnectionResult result) {
                                Log.i(TAG, "Connection failed. Cause: " + result.toString());
                                System.out.println("Connected 4");
                                if (!result.hasResolution()) {
                                    GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                                            Run_HistoryActivity.this, 0).show();
                                    return;
                                }
                                if (!authInProgress) {
                                    try {
                                        Log.i(TAG, "Attempting to resolve failed connection");
                                        System.out.println("Connected 5");
                                        authInProgress = true;
                                        result.startResolutionForResult(Run_HistoryActivity.this,
                                                REQUEST_OAUTH);
                                    } catch (IntentSender.SendIntentException e) {
                                        Log.e(TAG,
                                                "Exception while starting resolution activity", e);
                                        System.out.println("Connected 6");
                                    }
                                }
                            }
                        }
                )
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect to the Fitness API
        Log.i(TAG, "Connecting...");
        mClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mClient.isConnected()) {
            mClient.disconnect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mClient.isConnecting() && !mClient.isConnected()) {
                    mClient.connect();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }
    private class InsertAndVerifyDataTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... result) {
            DataReadRequest readRequest = queryFitnessData();
            DataReadResult dataReadResult =
                    Fitness.HistoryApi.readData(mClient, readRequest).await(1, TimeUnit.MINUTES);
            printData(dataReadResult);
            return result[0];
        }
        protected void onPostExecute(String result) {
            System.out.println("Steps taken: " + tmp);
        }
    }

    /**
     * Create and return a {@link DataSet} of step count data for the History API.
     */
    private DataReadRequest queryFitnessData() {
        // [START build_read_data_request]
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.MINUTE, -Math.round(timeWindow));
        long startTime = cal.getTimeInMillis();

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
        Log.i(TAG, "Range End: " + dateFormat.format(endTime));

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(Math.round(timeWindow), TimeUnit.MINUTES)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        return readRequest;
    }
    private void printData(DataReadResult dataReadResult) {
        if (dataReadResult.getBuckets().size() > 0) {
            Log.i(TAG, "Number of returned buckets of DataSets is: "
                    + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.i(TAG, "Number of returned DataSets is: "
                    + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }
    }

    private void dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
                tmp = "" + dp.getValue(field);
                System.out.println("Steps taken: " + tmp);
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_history, menu);
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
    public void saveToDataBase()
    {

    }


}