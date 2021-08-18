package com.example.orbitsatellitevisualizer;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.orbitsatellitevisualizer.R;
import com.example.orbitsatellitevisualizer.create.utility.model.Action;
import com.example.orbitsatellitevisualizer.create.utility.model.ActionController;
import com.example.orbitsatellitevisualizer.create.utility.model.balloon.Balloon;
import com.example.orbitsatellitevisualizer.create.utility.model.poi.POICamera;
import com.example.orbitsatellitevisualizer.create.utility.model.shape.Shape;
import com.example.orbitsatellitevisualizer.dialog.CustomDialog;
import com.example.orbitsatellitevisualizer.dialog.CustomDialogUtility;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class DemoThread implements Runnable {

    private static final String TAG_DEBUG = "TestStoryboardThread";

    private final AtomicBoolean running = new AtomicBoolean(false);
    private String message;
    private AppCompatActivity activity;
    private Dialog dialog;
    Timer t = new Timer();


    DemoThread(String message, AppCompatActivity activity, Dialog dialog){
        this.message = message;
        this.activity = activity;
        this.dialog = dialog;
    }

    void start() {
        Thread worker = new Thread(this);
        worker.start();
    }

    void stop() {
        running.set(false);
    }

    @Override
    public void run() {
        running.set(true);
        System.out.println("HERE IN RUN!: String: " + message);
        t.scheduleAtFixedRate(new TimerTask() {
                                  @Override
                                  public void run() {
                                      System.out.println("Checking");
                                      ActionController.getInstance().cleanFileKMLs(0);
                                      System.out.println("SENDMESSAGE output: " + message);
                                  }
                              },
                0,
                15000);

    }
}
