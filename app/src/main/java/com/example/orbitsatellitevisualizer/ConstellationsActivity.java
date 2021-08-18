package com.example.orbitsatellitevisualizer;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.FileUtils;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.orbitsatellitevisualizer.connection.LGCommand;
import com.example.orbitsatellitevisualizer.create.utility.model.Action;
import com.example.orbitsatellitevisualizer.create.utility.model.ActionController;
import com.example.orbitsatellitevisualizer.create.utility.model.ActionBuildCommandUtility;
//import com.example.orbitsatellitevisualizer.web_scraping.WebScrapingCelestrak;
import com.example.orbitsatellitevisualizer.TopBarActivity;
import com.example.orbitsatellitevisualizer.create.utility.model.poi.POI;
import com.example.orbitsatellitevisualizer.utility.ConstantPrefs;

import com.neosensory.tlepredictionengine.TlePredictionEngine;
import com.neosensory.tlepredictionengine.Tle;
import com.neosensory.tlepredictionengine.ElsetRec;
import com.neosensory.tlepredictionengine.Sgp4;
import com.neosensory.tlepredictionengine.TemeGeodeticConverter;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.Date;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;


import com.example.orbitsatellitevisualizer.R;

public class ConstellationsActivity extends TopBarActivity {

    private static final String TAG_DEBUG = "ConstellationActivity";

    private Dialog dialog;
    //private DemoThread demoThread = null;
    private Handler handler = new Handler();
    private TextView connectionStatus;
    private List<Action> actionsSaved = new ArrayList<>();
    private Button buttDemo;
    private Handler mHandler = new Handler();
    public static final String EXTRA_MESSAGE = "com.example.orbitsatellitevisualizer.MESSAGE";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constellations);

        View topBar = findViewById(R.id.top_bar);
        buttDemo = topBar.findViewById(R.id.butt_demo2);
        connectionStatus = findViewById(R.id.connection_status);

        SharedPreferences sharedPreferences = getSharedPreferences(ConstantPrefs.SHARED_PREFS.name(), MODE_PRIVATE);
        loadConnectionStatus(sharedPreferences);
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

    public void sendStarlinkConst(View view) {
        ActionController.getInstance().sendStarlinkConstFile(ConstellationsActivity.this);
    }

    public void sendIridiumConst(View view) {
        ActionController.getInstance().sendIridiumConstFile(ConstellationsActivity.this);
    }

    private Runnable createRunnableStarlink(final String paramStr){

        Runnable StarlinkRunnable = new Runnable(){
            public void run(){
                ActionController.getInstance().cleanFileKMLs(0);
                System.out.println("IN RUNNABLE");
                System.out.println("SENDMESSAGE output: " + paramStr);
                ActionController.getInstance().sendLiveGroup(ConstellationsActivity.this, paramStr);
                mHandler.postDelayed(this, 30000);
            }
        };
        return StarlinkRunnable;
    }

    public void sendStarlinkLive(View view) {
        /*
        Intent intent = new Intent(this, ConstellationsActivity.class);
        EditText editText = (EditText) findViewById(R.id.editTextTextPersonName);
        String message = editText.getText().toString();

        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
        */
        String message = "STARLINK";
        System.out.println("SENDMESSAGE output: " + message);
        ActionController.getInstance().sendLiveGroup(ConstellationsActivity.this, message);
        //Runnable myRunnable = createRunnableStarlink(message);
        //mHandler.postDelayed(myRunnable, 0);
    }

    public void sendIridiumLive(View view) {
        /*
        Intent intent = new Intent(this, ConstellationsActivity.class);
        EditText editText = (EditText) findViewById(R.id.editTextTextPersonName);
        String message = editText.getText().toString();

        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
        */
        String message = "IRIDIUM";
        System.out.println("SENDMESSAGE output: " + message);
        ActionController.getInstance().sendLiveGroup(ConstellationsActivity.this, message);
        //Runnable myRunnable = createRunnableStarlink(message);
        //mHandler.postDelayed(myRunnable, 0);
    }
}
