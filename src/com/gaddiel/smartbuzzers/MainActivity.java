package com.gaddiel.smartbuzzers;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity implements OnMapLongClickListener, OnClickListener {
    private Button btn_CancelAlarm;
    private Button btn_ConfirmLocation;
    private Button btn_find;
    private EditText etLocation;
    private GoogleMap googleMap;
    private LatLng latLng;
    private Marker marker;
    MarkerOptions markerOptions;
    private TextView tvMsg;

    /* renamed from: com.akashsebastian.smartbuzzer.MainActivity.1 */
    class C00521 implements DialogInterface.OnClickListener {
        C00521() {
        }

        public void onClick(DialogInterface dialogInterface, int i) {
        }
    }

    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {
        private boolean timeoutHappened;

        private GeocoderTask() {
            this.timeoutHappened = false;
        }

        protected List<Address> doInBackground(String... locationName) {
            List<Address> addresses = null;
            try {
                addresses = new Geocoder(MainActivity.this.getBaseContext()).getFromLocationName(locationName[0], 3);
                this.timeoutHappened = false;
                return addresses;
            } catch (IOException e) {
                Log.d("MainActivity.doInBackground()", "IOException: " + e.getMessage());
                this.timeoutHappened = true;
                e.printStackTrace();
                return addresses;
            }
        }

        protected void onPostExecute(List<Address> addresses) {
            if (this.timeoutHappened) {
                Toast.makeText(MainActivity.this.getBaseContext(), "Both Location and Internet service should be turned on", 1).show();
                Log.d("MainActivity.onPostExecute()", "addresses is null");
            } else if (addresses == null || addresses.size() == 0) {
                Toast.makeText(MainActivity.this.getBaseContext(), "No Location found", 1).show();
                Log.d("MainActivity.onPostExecute()", "addresses is null");
            } else {
                MainActivity.this.googleMap.clear();
                for (int i = 0; i < addresses.size(); i++) {
                    Address address = (Address) addresses.get(i);
                    MainActivity.this.latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    String str = "%s, %s";
                    Object[] objArr = new Object[2];
                    objArr[0] = address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "";
                    objArr[1] = address.getCountryName();
                    String addressText = String.format(str, objArr);
                    MainActivity.this.markerOptions = new MarkerOptions();
                    MainActivity.this.markerOptions.position(MainActivity.this.latLng);
                    MainActivity.this.markerOptions.title(addressText);
                    MainActivity.this.marker = MainActivity.this.googleMap.addMarker(MainActivity.this.markerOptions);
                    if (i == 0) {
                        MainActivity.this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MainActivity.this.latLng, 13.0f));
                    }
                }
            }
        }
    }

    public MainActivity() {
        this.latLng = new LatLng(0.0d, 0.0d);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity.onCreate()", "Start");
        setContentView(R.layout.activity_main);
        this.btn_ConfirmLocation = (Button) findViewById(R.id.bConfirm_loc);
        this.btn_CancelAlarm = (Button) findViewById(R.id.bCancel_loc);
        this.btn_find = (Button) findViewById(R.id.btn_find_loc);
        this.tvMsg = (TextView) findViewById(R.id.tvMsg);
        EditText etLocation = (EditText) findViewById(R.id.et_location);
        this.btn_CancelAlarm.setVisibility(8);
        this.btn_ConfirmLocation.setVisibility(8);
        this.googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_view)).getMap();
        this.googleMap.setMyLocationEnabled(true);
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        Location currentLocation = BuzzerReceiver.getLastKnownLocation((LocationManager) getSystemService("location"));
        if (currentLocation != null) {
            this.latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            Log.d("MainActivity.onCreate()", this.latLng.toString());
            this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(this.latLng, 10.0f));
        } else {
            Log.d("MainActivity.onCreate()", "Unable to Location using getMyLocation()" + this.latLng.toString());
        }
        this.btn_ConfirmLocation.setOnClickListener(this);
        this.btn_CancelAlarm.setOnClickListener(this);
        this.btn_find.setOnClickListener(this);
        this.googleMap.setOnMapLongClickListener(this);
        LocationInfo locationInfo = new SQLiteOperations(this).getTargetLocation();
        if (locationInfo.range != -1) {
            Log.d("MainActivity.onCreate()", "Alarm is set already; LoctionInfo object is NOT null");
            String targetPosition = new StringBuilder(String.valueOf(String.valueOf(locationInfo.lat))).append(",").append(String.valueOf(locationInfo.lng)).toString();
            new BackgroundTasks(getApplicationContext()).doInBackground(String.valueOf(locationInfo.range), targetPosition);
            if (this.marker != null) {
                this.marker.remove();
            }
            this.latLng = new LatLng(locationInfo.lat, locationInfo.lng);
            this.marker = this.googleMap.addMarker(new MarkerOptions().position(this.latLng).title("Target").snippet("Reach").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            this.btn_ConfirmLocation.setVisibility(8);
            this.btn_CancelAlarm.setVisibility(0);
        } else {
            Log.d("MainActivity.onCreate()", "No alarm was set prior; LoctionInfo object is null");
        }
        checkIfGPSIsOn();
        Log.d("MainActivity.onCreate()", "Done");
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onClick(View v) {
    	 Log.d("checkvalue", "Done"+v.getId());
    	 Log.d("checkvalue", "find"+R.id.btn_find_loc);
    	 Log.d("checkvalue", "cancel"+R.id.bCancel_loc);
    	 Log.d("checkvalue", "confirm"+R.id.bConfirm_loc);
    	
        switch (v.getId()) {
            case R.id.btn_find_loc:            	
                findLocation();     
                break;
            case R.id.bCancel_loc:            	 
            	 setupCancelAlarm();
            	 break;
            case R.id.bConfirm_loc:            	
                setupConfirmAlarm();
                break;
            default:
        }
    }

    private void findLocation() {
        String location = ((EditText) findViewById(R.id.et_location)).getText().toString();
        if (location != null && !location.equals("")) {
            new GeocoderTask().execute(new String[]{location});
        }
    }

    public void onMapLongClick(LatLng latLng) {
        Log.d("MainActivity.OnMapClick()", "Clicked location is: " + latLng.toString());
        if (this.marker != null) {
            this.marker.remove();
        }
        this.marker = this.googleMap.addMarker(new MarkerOptions().position(latLng).title("Target").snippet("Reach").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        this.latLng = latLng;
        this.btn_ConfirmLocation.setText("Click Here To Set Alarm");
        this.btn_ConfirmLocation.setVisibility(0);
        this.btn_CancelAlarm.setVisibility(8);
    }

    public void setupConfirmAlarm() {
        if (this.latLng == null) {
            Log.d("MainActivity.setupConfirmAlarm()", "latLng is null");
        }
        Intent alarmUIIntent = new Intent("com.gaddiel.smartbuzzers.ALARMUI");
        alarmUIIntent.putExtra("targetPositionLat", this.latLng.latitude);
        alarmUIIntent.putExtra("targetPositionLong", this.latLng.longitude);
        Log.d("MainActivity.setupConfirmAlarm()", "Lat:" + this.latLng.latitude + ", Lng" + this.latLng.longitude);
        this.btn_ConfirmLocation.setVisibility(8);
        this.btn_CancelAlarm.setVisibility(0);
        startActivity(alarmUIIntent);
    }

    public void setupCancelAlarm() {
        if (this.marker != null) {
            this.marker.remove();
            this.marker = null;
            this.markerOptions = null;
        }
        SQLiteOperations sQLiteOperations = new SQLiteOperations(this);
        sQLiteOperations.deleteTargetLocation();
        sQLiteOperations.insertCancelAlarm();
        Log.d("MainActivity.setupCancelAlarm()", "CancelAlarm value: " + sQLiteOperations.isCancelAlarmSet());
        this.btn_ConfirmLocation.setVisibility(8);
        this.btn_CancelAlarm.setVisibility(8);
    }

    private void checkIfGPSIsOn() {
        LocationManager lm = (LocationManager) getSystemService("location");
        if (!lm.isProviderEnabled("gps") || !lm.isProviderEnabled("network")) {
            Builder builder = new Builder(this);
            builder.setTitle("Location Services Not Active");
            builder.setMessage("GPS / Location service must be turned on for the App to work.");
            builder.setPositiveButton("OK", new C00521());
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }
}
