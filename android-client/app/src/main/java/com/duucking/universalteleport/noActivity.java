package com.duucking.universalteleport;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.duucking.universalteleport.service.AppService;

public class noActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_no);
        Log.e("UniversalTeleportTest", "no activity start:");
        startForegroundService(new Intent(this, AppService.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }
}