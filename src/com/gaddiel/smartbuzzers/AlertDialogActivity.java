package com.gaddiel.smartbuzzers;

import android.app.Activity;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class AlertDialogActivity extends Activity implements OnClickListener {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_ui);
        ((Button) findViewById(R.id.bOk)).setOnClickListener(this);
    }

    public void onClick(View v) {
        Toast.makeText(getBaseContext(), "Thank you for using this product.", 0).show();
        Log.d("AlertDialogActivity.onClick()", "Killing ...");
        Process.killProcess(Process.myPid());
    }
}
