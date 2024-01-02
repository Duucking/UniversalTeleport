package com.duucking.universalteleport;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.duucking.universalteleport.util.TeleportUtil;

import java.io.IOException;

public class ShareTextActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_share_text);
        Log.e("UniversalTeleportTest", "activity oncreate");
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            if (extras.containsKey(Intent.EXTRA_TEXT)) {
                try {
                    String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                    Log.e("UniversalTeleportTest", "share text:" + text);
                    SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
                    String address = sharedPreferences.getString("ip", "");
                    if (address.equals("")) {
                        Toast.makeText(this, "未设置IP，需要先设置呢", Toast.LENGTH_SHORT).show();
                    } else {
                        Thread thread = new Thread(new sendTCPThread(address, 8556, text));
                        thread.start();
                    }
                    finish();
                } catch (Exception e) {
                    Log.e(this.getClass().getName(), e.toString());
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public class sendTCPThread implements Runnable {
        private String address;
        private int port;
        private String text;

        public sendTCPThread(String address, int port, String text) {
            this.address = address;
            this.port = port;
            this.text = text;
        }

        @Override
        public void run() {
            try {
                TeleportUtil.sendTCPMessage(address, port, text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}