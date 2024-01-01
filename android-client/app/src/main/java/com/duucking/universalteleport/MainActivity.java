package com.duucking.universalteleport;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.duucking.universalteleport.service.AppService;


public class MainActivity extends AppCompatActivity {
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
        key_edit = findViewById(R.id.keyinput);
        ip_edit = findViewById(R.id.ipinput);
        key_edit.setText(key);
        ip_edit.setText(ip);
        findViewById(R.id.button).setOnClickListener(v -> {
            String key_data = key_edit.getText().toString();
            String ip_data = ip_edit.getText().toString();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("key", key_data);
            editor.putString("ip", ip_data);
            editor.apply();
            Toast.makeText(this, "设置成功了！", Toast.LENGTH_SHORT).show();
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
}