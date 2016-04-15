package com.gaddiel.smartbuzzers;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AlarmUI extends Activity {
    private static String targetPosition;
    private static double targetPositionLat;
    private static double targetPositionLong;
    private final BroadcastReceiver mReceivedSMSReceiver;
    private int range;

    /* renamed from: com.akashsebastian.smartbuzzer.AlarmUI.1 */
    class C00471 extends BroadcastReceiver {
        C00471() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            AlarmUI.this.displayAlert();
        }
    }

    /* renamed from: com.akashsebastian.smartbuzzer.AlarmUI.2 */
    class C00482 implements OnClickListener {
        private final /* synthetic */ EditText val$etRange;

        C00482(EditText editText) {
            this.val$etRange = editText;
        }

        public void onClick(View v) {
            Log.d("AlarmUI", "Entered Range Value Km: " + this.val$etRange.getText());
            if (this.val$etRange.getText().toString().equals("")) {
                AlarmUI.this.range = -1;
            } else {
                AlarmUI.this.range = Integer.parseInt(this.val$etRange.getText().toString());
            }
            if (AlarmUI.this.range > 1) {
                String rangeTxtStr = this.val$etRange.getText().toString();
                new BackgroundTasks(AlarmUI.this.getApplicationContext()).doInBackground(rangeTxtStr, AlarmUI.targetPosition);
                new SQLiteOperations(AlarmUI.this.getApplicationContext()).insertTargetLocation(new LocationInfo("Cityname", AlarmUI.targetPositionLat, AlarmUI.targetPositionLong, AlarmUI.this.range));
                AlarmUI.this.finish();
                return;
            }
            Toast toast = Toast.makeText(AlarmUI.this.getApplicationContext(), "Enter a value 2km or above", 0);
            toast.setGravity(17, 0, 0);
            toast.show();
            Log.d("AlarmUI.onClick()", "0 or -ve value entered for range: " + AlarmUI.this.range);
        }
    }

    /* renamed from: com.akashsebastian.smartbuzzer.AlarmUI.3 */
    class C00493 implements DialogInterface.OnClickListener {
        C00493() {
        }

        public void onClick(DialogInterface dialog, int id) {
            dialog.cancel();
        }
    }

    /* renamed from: com.akashsebastian.smartbuzzer.AlarmUI.4 */
    class C00504 implements DialogInterface.OnClickListener {
        C00504() {
        }

        public void onClick(DialogInterface dialog, int id) {
            dialog.cancel();
        }
    }

    public AlarmUI() {
        this.range = 0;
        this.mReceivedSMSReceiver = new C00471();
    }

    static {
        targetPositionLat = 0.0d;
        targetPositionLong = 0.0d;
        targetPosition = null;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_ui);
        Intent myIntent = getIntent();
        targetPositionLat = myIntent.getDoubleExtra("targetPositionLat", 0.0d);
        targetPositionLong = myIntent.getDoubleExtra("targetPositionLong", 0.0d);
        Log.d("AlarmUI", "TargetPosition is: " + targetPositionLat + ", " + targetPositionLong);
        targetPosition = new StringBuilder(String.valueOf(String.valueOf(targetPositionLat))).append(",").append(String.valueOf(targetPositionLong)).toString();
        ((Button) findViewById(R.id.bSetAlarm)).setOnClickListener(new C00482((EditText) findViewById(R.id.etRange)));
    }

    private void displayAlert() {
        Builder builder = new Builder(this);
        builder.setMessage("You have reached the destination").setCancelable(false).setPositiveButton("Yes", new C00493()).setNegativeButton("No", new C00504());
        builder.create().show();
    }
}
