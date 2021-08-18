package com.example.orbitsatellitevisualizer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.orbitsatellitevisualizer.create.utility.model.Action;
import com.example.orbitsatellitevisualizer.create.utility.model.ActionController;
import com.example.orbitsatellitevisualizer.dialog.CustomDialogUtility;
import com.example.orbitsatellitevisualizer.utility.ConstantPrefs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SingleSpacecraftsActivity extends TopBarActivity {

    private static final String TAG_DEBUG = "SingleSpacecraftsActivity";

    private Dialog dialog;
    //private DemoThread demoThread = null;
    private Handler handler = new Handler();
    private TextView connectionStatus;
    private List<Action> actionsSaved = new ArrayList<>();
    private Button buttDemo;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_spacecrafts);

        View topBar = findViewById(R.id.top_bar);
        buttDemo = topBar.findViewById(R.id.butt_spaceports);
        connectionStatus = findViewById(R.id.connection_status);


        SharedPreferences sharedPreferences = getSharedPreferences(ConstantPrefs.SHARED_PREFS.name(), MODE_PRIVATE);

    }

    /**
     * Run the demo that is pre saved
     */

    private String readDemoFile() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("demo.txt"), StandardCharsets.UTF_8));

            StringBuilder string = new StringBuilder();
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                string.append(mLine);
            }
            return string.toString();
        } catch (IOException e) {
            Log.w(TAG_DEBUG, "ERROR READING FILE: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.w(TAG_DEBUG, "ERROR CLOSING: " + e.getMessage());
                }
            }
        }
        return "";
    }

    /**
     * It gives a dialog with a cancel button
     */
    private void getDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SingleSpacecraftsActivity.this);
        @SuppressLint("InflateParams") View v = getLayoutInflater().inflate(R.layout.dialog_fragment, null);
        v.getBackground().setAlpha(220);
        Button ok = v.findViewById(R.id.ok);
        ok.setText(getResources().getString(R.string.stop_demo));
        ok.setOnClickListener(view -> {

            CustomDialogUtility.showDialog(SingleSpacecraftsActivity.this, getResources().getString(R.string.stop_message));
        });
        TextView textMessage = v.findViewById(R.id.message);
        textMessage.setText(getResources().getString(R.string.start_demo));
        textMessage.setTextSize(23);
        textMessage.setGravity(View.TEXT_ALIGNMENT_CENTER);
        builder.setView(v);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
    }


    public void sendStarlink(View view) {
        ActionController.getInstance().sendStarlinkfile(SingleSpacecraftsActivity.this);
    }

    public void sendEnxaneta(View view) {
        ActionController.getInstance().sendEnxanetaFile(SingleSpacecraftsActivity.this);
    }

    public void sendISS(View view) {
        ActionController.getInstance().sendISSfile(SingleSpacecraftsActivity.this);
    }
}
