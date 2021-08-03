package com.example.orbitsatellitevisualizer;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.orbitsatellitevisualizer.create.utility.model.ActionController;

import android.os.Handler;

public class CatalogActivity extends TopBarActivity {
    private static final String TAG_DEBUG = "CatalogActivity";
    public static final String EXTRA_MESSAGE = "com.example.orbitsatellitevisualizer.MESSAGE";
    private Dialog dialog;
    private Button buttCatalog;
    private Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        View topBar = findViewById(R.id.top_bar);
        buttCatalog = topBar.findViewById(R.id.butt_scn);
    }

    /* FUNCIONAL, SENSE PERIODIC REPEAT
    public void sendMessage(View view) {
        Intent intent = new Intent(this, CatalogActivity.class);
        EditText editText = (EditText) findViewById(R.id.editTextTextPersonName);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
        System.out.println("sendMessage output: " + message);

        ActionController.getInstance().sendLiveSCN(CatalogActivity.this, message);
        //ActionController.getInstance().orbit(poi, null);
    }
    */

    private Runnable createRunnable(final String paramStr){

        Runnable aRunnable = new Runnable(){
            public void run(){
                ActionController.getInstance().cleanFileKMLs(0);
                System.out.println("IN RUNNABLE");
                System.out.println("SENDMESSAGE output: " + paramStr);
                ActionController.getInstance().sendLiveSCN(CatalogActivity.this, paramStr);
                mHandler.postDelayed(this, 30000);
            }
        };
        return aRunnable;
    }

    public void sendMessage(View view) {
        Intent intent = new Intent(this, CatalogActivity.class);
        EditText editText = (EditText) findViewById(R.id.editTextTextPersonName);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
        System.out.println("SENDMESSAGE output: " + message);
        ActionController.getInstance().sendLiveSCN(CatalogActivity.this, message);
        Runnable myRunnable = createRunnable(message);
        mHandler.postDelayed(myRunnable, 0);
    }
}