package com.gaddiel.smartbuzzers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.drive.DriveFile;
import java.util.Calendar;

public class BackgroundTasks extends AsyncTask<String, Void, String> {
    private static int PARAM_COUNT;
    private Context applicationContext;

    static {
        PARAM_COUNT = 2;
    }

    public BackgroundTasks(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    protected String doInBackground(String... params) {
        Log.d("BackgroundTasks: doInBackground", "Start");
        try {
            if (params.length != PARAM_COUNT) {
                Log.d("BackgroundTasks: doInBackground: Error", "Need " + PARAM_COUNT + " params for BackgroundTasks. Found only " + params.length);
            }
            String range = params[0];
            String targetPosition = params[1];
            Log.d("BackgroundTasks: doInBackground: ", "range: " + range + ", targetPosition: " + targetPosition);
            setupBuzzerCheckAlarm(range, targetPosition);
            Log.d("BackgroundTasks: doInBackground", "End");
            return "Executed";
        } catch (Exception e) {
            Log.e("LongOperation", "Interrupted", e);
            return "Interrupted";
        }
    }

    private void setupBuzzerCheckAlarm(String range, String targetPosition) {
        Log.d("BackgroundTasks: setupBuzzerCheckAlarm", "Start");
        Intent myIntentTest = new Intent(this.applicationContext, BuzzerReceiver.class);
        PendingIntent pendingUpdateIntent = PendingIntent.getBroadcast(this.applicationContext, 100, myIntentTest, DriveFile.MODE_WRITE_ONLY);
        Log.d("BackgroundTasks: setupBuzzerCheckAlarm", "Canceling the BuzzerReceiver alarm");
        try {
            ((AlarmManager) this.applicationContext.getSystemService("alarm")).cancel(pendingUpdateIntent);
            Log.d("BackgroundTasks: setupBuzzerCheckAlarm", "Canceled the BuzzerReceiver Alarm");
        } catch (Exception e) {
            Log.e("BackgroundTasks: setupBuzzerCheckAlarm Error", "BuzzerReceiver is not canceled. " + e.toString());
        }
        Log.d("BackgroundTasks: setupBuzzerCheckAlarm", "BuzzerReceiver alarm is canceled; Creating it");
        myIntentTest.putExtra("targetPosition", targetPosition);
        myIntentTest.putExtra("range", range);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.applicationContext, BuzzerReceiver.BUZZER_RECEIVER_ALARM_CODE, myIntentTest, 0);
        Log.d("BackgroundTasks: setupBuzzerCheckAlarm", "interval timeval=" + 40000);
        ((AlarmManager) this.applicationContext.getSystemService("alarm")).setRepeating(1, Calendar.getInstance().getTimeInMillis(), 40000, pendingIntent);
        Log.d("BackgroundTasks: setupBuzzerCheckAlarm", "Done");
    }

    protected void onPostExecute(String result) {
    }
}
