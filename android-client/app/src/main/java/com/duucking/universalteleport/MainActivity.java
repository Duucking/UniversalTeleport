package com.duucking.universalteleport;


import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.duucking.universalteleport.service.AppService;
import com.duucking.universalteleport.service.QuickActionService;

import java.util.Random;


public class MainActivity extends AppCompatActivity {
    private EditText name_edit;
    private EditText key_edit;
    private EditText ip_edit;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startForegroundService(new Intent(this, AppService.class));
        } else {
            startService(new Intent(this, AppService.class));
        }
        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        String key = sharedPreferences.getString("key", "");
        String ip = sharedPreferences.getString("ip", "");
        if (sharedPreferences.getString("deviceName", "").equals("")) {
            String[] names1 = {"Beautiful ", "Slowly ", "Strong ", "Cute ", "Kawaii ", "Big ", "Small ", "Alert ", "Gentle ", "Dependable "};
            String[] names2 = {"Butterfly", "Snail", "Goldfish", "Cat", "Penguin", "Chicken", "Bird", "Hedgehog", "Snake"};
            Random random = new Random();
            String name1 = names1[random.nextInt(names1.length)];
            String name2 = names2[random.nextInt(names2.length)];
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("deviceName", name1 + name2);
            editor.apply();
        }
        String deviceName = sharedPreferences.getString("deviceName", "");
        name_edit = findViewById(R.id.nameinput);
        key_edit = findViewById(R.id.keyinput);
        ip_edit = findViewById(R.id.ipinput);
        name_edit.setText(deviceName);
        key_edit.setText(key);
        ip_edit.setText(ip);
        findViewById(R.id.button).setOnClickListener(v -> {
            String name_data = name_edit.getText().toString();
            String key_data = key_edit.getText().toString();
            String ip_data = ip_edit.getText().toString();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("deviceName", name_data);
            editor.putString("key", key_data);
            editor.putString("ip", ip_data);
            editor.apply();
            Toast.makeText(this, getString(R.string.main_activity_toast_text), Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TileService.requestListeningState(this, new ComponentName(BuildConfig.APPLICATION_ID, QuickActionService.class.getName()));
        Log.e("UniversalTeleportTest", "Mainactivity destroy");
    }
}