package com.gaddiel.smartbuzzers;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class BuzzerReceiver extends BroadcastReceiver {
    public static int BUZZER_RECEIVER_ALARM_CODE;
    private static Location currentLocation;
    Context context;
    String loc;
    private final LocationListener locationListener;

    /* renamed from: com.akashsebastian.smartbuzzer.BuzzerReceiver.1 */
    class C00511 implements LocationListener {
        C00511() {
        }

        public void onLocationChanged(Location location) {
            BuzzerReceiver.this.updateWithNewLocation(location);
        }

        public void onProviderDisabled(String provider) {
            BuzzerReceiver.this.updateWithNewLocation(null);
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    public BuzzerReceiver() {
        this.locationListener = new C00511();
    }

    static {
        currentLocation = null;
        BUZZER_RECEIVER_ALARM_CODE = 100;
    }

	public void onReceive(Context context, Intent myIntent) {
        this.context = context;
        Log.d("BuzzerReceiver: onReceive", "Start");
        Bundle extras = myIntent.getExtras();
        String thisTargetPosition = extras.getString("targetPosition");
        String thisRange = extras.getString("range");
        Log.i("BuzzerReceiver: onReceive", "thisTargetPosition: " + thisTargetPosition);
        Log.i("BuzzerReceiver: onReceive", "thisRange: " + thisRange);
        SQLiteOperations sQLiteOperations = new SQLiteOperations(this.context);
        if (sQLiteOperations.isCancelAlarmSet()) {
            Log.d("BuzzerReceiver: onReceive", "Cancel Alarm is set by the user, Canceling the system alarm ");
            cancelAlarm();
            sQLiteOperations.deleteCancelAlarm();
        } else {
            doTask(thisTargetPosition, thisRange);
        }
        Log.d("BuzzerReceiver: onReceive", "End");
    }

    private void doTask(String targetPosition, String range) {
        Log.d("BuzzerReceiver: doTask", "Start");
        Log.i("BuzzerReceiver: doTask", "targetPosition: " + targetPosition);
        Log.i("BuzzerReceiver: doTask", "range: " + range);
        if (targetPosition == null || range == null) {
            Log.i("BuzzerReceiver: doTask", "targetPosition is null or range is less than 0");
            return;
        }
        LocationManager locationManager = (LocationManager) this.context.getSystemService("location");
        Criteria criteria = new Criteria();
        criteria.setAccuracy(1);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(1);
        String provider = locationManager.getBestProvider(criteria, true);
        currentLocation = getLastKnownLocation(locationManager);
        locationManager.requestLocationUpdates(provider, 2000, 10.0f, this.locationListener);
        if (currentLocation == null) {
            Log.d("BuzzerReceiver: doTask", "Current location is null. Returning");
            return;
        }
        Log.d("BuzzerReceiver: doTask", "Current location is " + currentLocation.toString());
        SQLiteOperations sQLiteOperations = new SQLiteOperations(this.context);
        LocationInfo locationInfo = sQLiteOperations.getTargetLocation();
        double targetLat = locationInfo.lat;
        double targetLng = locationInfo.lng;
        int dbRange = locationInfo.range;
        LatLng latLng = new LatLng(targetLat, targetLng);
        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        LatLngBounds bounds = getLatLngBounds(latLng, dbRange);
        Log.d("BuzzerReceiver: doTask", "bounds is " + bounds.toString());
        if (bounds.contains(currentLatLng)) {
            RingtoneManager.getRingtone(this.context, RingtoneManager.getDefaultUri(1)).play();
            Log.d("BuzzerReceiver: doTask", "True... Alarm is ringing");
            long[] pattern = new long[18];
            pattern[1] = 2500;
            pattern[2] = 1000;
            pattern[3] = 2500;
            pattern[4] = 1000;
            pattern[5] = 2500;
            pattern[6] = 1000;
            pattern[7] = 2500;
            pattern[8] = 1000;
            pattern[9] = 2500;
            pattern[10] = 1000;
            pattern[11] = 2500;
            pattern[12] = 1000;
            pattern[13] = 2500;
            pattern[14] = 1000;
            pattern[15] = 2500;
            pattern[16] = 1000;
            pattern[17] = 2500;
            ((Vibrator) this.context.getSystemService("vibrator")).vibrate(pattern, -1);
            Log.d("BuzzerReceiver: doTask", "Canceling the alarm");
            cancelAlarm();
            sQLiteOperations.deleteTargetLocation();
            Intent alertDialogActivity = new Intent("com.gaddiel.smartbuzzers.ALERTDIALOGACTIVITY");
            alertDialogActivity.addFlags(DriveFile.MODE_READ_ONLY);
            this.context.startActivity(alertDialogActivity);
        } else {
            Log.d("BuzzerReceiver: doTask", "Not within bounds. No alarm");
        }
        Log.d("BuzzerReceiver: doTask", "End");
    }

    private void cancelAlarm() {
        Log.d("BuzzerReceiver: cancelAlarm", "Start");
        PendingIntent pendingUpdateIntent = PendingIntent.getBroadcast(this.context, BUZZER_RECEIVER_ALARM_CODE, new Intent(this.context, BuzzerReceiver.class), DriveFile.MODE_WRITE_ONLY);
        if (pendingUpdateIntent != null) {
            try {
                ((AlarmManager) this.context.getSystemService("alarm")).cancel(pendingUpdateIntent);
                Log.d("BuzzerReceiver: cancelAlarm", "Canceled the Alarm");
            } catch (Exception e) {
                Log.e("BuzzerReceiver: cancelAlarm", "Exception while canceling the alarm. " + e.toString());
            }
        }
        Log.d("BuzzerReceiver: cancelAlarm", "End");
    }

    public static Location getLastKnownLocation(LocationManager locationManager) {
        Location bestLocation = null;
        for (String provider : locationManager.getProviders(true)) {
            Location l = locationManager.getLastKnownLocation(provider);
            Log.d("BuzzerReceiver: getLastKnownLocation", "last known location, provider: " + provider + ", location" + l);
            if (l != null && (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy())) {
                Log.d("BuzzerReceiver: getLastKnownLocation", "Found best last known location: " + l);
                bestLocation = l;
            }
        }
        if (bestLocation == null) {
            return null;
        }
        return bestLocation;
    }

    private LatLngBounds getLatLngBounds(LatLng targetLatLng, int range) {
        LatLng latLng1 = new LatLng(targetLatLng.latitude + (((double) range) * 0.0072728d), targetLatLng.longitude + (((double) range) * 0.0072728d));
        LatLng latLng2 = new LatLng(targetLatLng.latitude - (((double) range) * 0.0072728d), targetLatLng.longitude - (((double) range) * 0.0072728d));
        LatLngBounds bounds = new LatLngBounds(latLng2, latLng1);
        Log.d("BuzzerReceiver: getLatLngBounds", "latLng2 is " + latLng2.toString());
        Log.d("BuzzerReceiver: getLatLngBounds", "latLng1 is " + latLng1.toString());
        Log.d("BuzzerReceiver: getLatLngBounds", "bounds is " + bounds.toString());
        return bounds;
    }

    private String updateWithNewLocation(Location location) {
        String latLongString;
        Log.d("BuzzerReceiver: updateWithNewLocation", "In updateWithNewLocation");
        if (location != null) {
            latLongString = new StringBuilder(String.valueOf(Double.toString(location.getLatitude()))).append(",").append(Double.toString(location.getLongitude())).toString();
        } else {
            latLongString = "NULL,NULL";
        }
        currentLocation = location;
        return latLongString;
    }
}
