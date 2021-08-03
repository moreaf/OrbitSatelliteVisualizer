package com.example.orbitsatellitevisualizer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SpaceportsActivity extends TopBarActivity {
    private static final String TAG_DEBUG = "SpaceportsActivity";
    public static final String EXTRA_MESSAGE = "com.example.orbitsatellitevisualizer.MESSAGE";
    private Dialog dialog;
    private Button buttSpaceports;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spaceports);
        View topBar = findViewById(R.id.top_bar);
        buttSpaceports = topBar.findViewById(R.id.butt_spaceports);
    }
}