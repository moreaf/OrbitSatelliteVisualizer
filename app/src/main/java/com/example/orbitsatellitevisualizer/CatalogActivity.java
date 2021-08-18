package com.example.orbitsatellitevisualizer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.orbitsatellitevisualizer.create.utility.connection.LGConnectionTest;
import com.example.orbitsatellitevisualizer.create.utility.model.Action;
import com.example.orbitsatellitevisualizer.create.utility.model.ActionController;
import com.example.orbitsatellitevisualizer.dialog.CustomDialogUtility;
import com.example.orbitsatellitevisualizer.utility.ConstantPrefs;

import android.os.Handler;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import java.util.Date;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


import static java.lang.Thread.sleep;

public class CatalogActivity extends TopBarActivity {
    private static final String TAG_DEBUG = "CatalogActivity";
    public static final String EXTRA_MESSAGE = "com.example.orbitsatellitevisualizer.MESSAGE";
    private Dialog dialog;
    private Button button9;
    private Button buttCatalog;
    private Handler mHandler = new Handler();
    private String scn;
    Timer t = new Timer();

    private DemoThread demoThread = null;
    private Handler handler = new Handler();
    private TextView connectionStatus;
    private Button buttDemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        View topBar = findViewById(R.id.top_bar);
        buttCatalog = topBar.findViewById(R.id.butt_scn);
        buttDemo = topBar.findViewById(R.id.butt_demo);
        connectionStatus = findViewById(R.id.connection_status);
        SharedPreferences sharedPreferences = getSharedPreferences(ConstantPrefs.SHARED_PREFS.name(), MODE_PRIVATE);
        loadConnectionStatus(sharedPreferences);
    }

    /* FUNCIONAL, SENSE PERIODIC REPEAT */
    public void sendMessage(View view) {
        ActionController.getInstance().cleanFileKMLs(0);
        Intent intent = new Intent(this, CatalogActivity.class);
        EditText editText = (EditText) findViewById(R.id.scn_field);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
        System.out.println("sendMessage output: " + message);

        handler.postDelayed(() -> {
            ActionController.getInstance().sendLiveSCN(CatalogActivity.this, message);
        }, 1000);
    }

    /*
    public void sendMessage(View view) {
        Intent intent = new Intent(this, CatalogActivity.class);
        EditText editText = (EditText) findViewById(R.id.scn_field);
        String message = editText.getText().toString();
        scn = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
        System.out.println("SENDMESSAGE output: " + message);
        ActionController.getInstance().sendLiveSCN(CatalogActivity.this, message);

        runDemo(message);
        /*
        t.scheduleAtFixedRate(new TimerTask() {
              @Override
              public void run() {
                  System.out.println("Checking");
                  ActionController.getInstance().cleanFileKMLs(0);
                  System.out.println("SENDMESSAGE output: " + scn);
                  ActionController.getInstance().sendLiveSCN(CatalogActivity.this, scn);
              }
          },
                0,
                10000);
    }
    */

    /**
     * Run the demo that is pre saved
     */
    private void runDemo(String message) {
        getDialog();
        System.out.println("IN RUN DEMO, message: " + message);

        AtomicBoolean isConnected = new AtomicBoolean(false);
        LGConnectionTest.testPriorConnection(this, isConnected);
        SharedPreferences sharedPreferences = getSharedPreferences(ConstantPrefs.SHARED_PREFS.name(), MODE_PRIVATE);
        handler.postDelayed(() -> {
            if (isConnected.get()) {
                dialog.show();
                demoThread = new DemoThread(message,CatalogActivity.this, dialog);
                demoThread.start();
            }
            loadConnectionStatus(sharedPreferences);
        }, 1200);

    }


    /**
     * It gives a dialog with a cancel button
     */
    private void getDialog() {
        System.out.println("IN GET DIALOG");
        AlertDialog.Builder builder = new AlertDialog.Builder(CatalogActivity.this);
        @SuppressLint("InflateParams") View v = getLayoutInflater().inflate(R.layout.dialog_fragment, null);
        v.getBackground().setAlpha(220);
        Button ok = v.findViewById(R.id.ok);
        ok.setText(getResources().getString(R.string.stop_demo));
        ok.setOnClickListener(view -> {
            stopDemo();
            CustomDialogUtility.showDialog(CatalogActivity.this, getResources().getString(R.string.stop_message));
        });
        TextView textMessage = v.findViewById(R.id.message);
        textMessage.setText(getResources().getString(R.string.start_demo));
        textMessage.setTextSize(23);
        textMessage.setGravity(View.TEXT_ALIGNMENT_CENTER);
        builder.setView(v);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
    }

    /**
     * Stop the demo
     */
    public void stopDemo() {
        demoThread.stop();
        dialog.dismiss();
    }

    /**
     * Set the connection status on the view
     */
    private void loadConnectionStatus(SharedPreferences sharedPreferences) {
        boolean isConnected = sharedPreferences.getBoolean(ConstantPrefs.IS_CONNECTED.name(), false);
        if (isConnected) {
            connectionStatus.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_status_connection_green));
        } else {
            connectionStatus.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_status_connection_red));
        }
    }

    @Override
    protected void onPause() {
        if (demoThread != null) stopDemo();
        demoThread = null;
        super.onPause();
    }

    public void onStop(View view) {
        if (demoThread != null) stopDemo();
        demoThread = null;
        super.onPause();
    }

    public void load_enxaneta(View v){
        String scn = "47954";
        TextView t = (TextView) findViewById(R.id.scn_field);
        t.setText(scn);
    }

    public void load_iss(View v){
        String scn = "25544";
        TextView t = (TextView) findViewById(R.id.scn_field);
        t.setText(scn);
    }

    public void load_starlink(View v){
        String scn = "44238";
        TextView t = (TextView) findViewById(R.id.scn_field);
        t.setText(scn);
    }

    public void load_iridium(View v){
        String scn = "24793";
        TextView t = (TextView) findViewById(R.id.scn_field);
        t.setText(scn);
    }

    public void load_tiangong(View v){
        String scn = "25544";
        TextView t = (TextView) findViewById(R.id.scn_field);
        t.setText(scn);
    }

    public void cleanLG(View view){
        ActionController.getInstance().cleanFileKMLs(0);
    }
}