package com.duucking.universalteleport;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.duucking.universalteleport.util.DataUtil;
import com.duucking.universalteleport.util.TeleportUtil;

import java.io.IOException;

public class ShareTextActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_share_text);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            assert extras != null;
            if (extras.containsKey(Intent.EXTRA_TEXT)) {
                try {
                    String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                    SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
                    String address = sharedPreferences.getString("ip", "");
                    String key = sharedPreferences.getString("key", "");
                    String encryptText = DataUtil.Encrypt(text, key);
                    if (encryptText == null) {
                        encryptText = text;
                    }
                    if (!address.equals("")) {
                        Thread thread = new Thread(new sendTCPThread(address, 8556, encryptText));
                        thread.start();
                    } else {
                        Thread uthread = new Thread(new sendUDPThread(8557, encryptText));
                        uthread.start();
                    }
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
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

    public static class sendTCPThread implements Runnable {
        private final String address;
        private final int port;
        private final String text;

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


    public static class sendUDPThread implements Runnable {
        private final int port;
        private final String text;

        public sendUDPThread(int port, String text) {
            this.port = port;
            this.text = text;
        }

        @Override
        public void run() {
            try {
                TeleportUtil.sendUDPMessage(port, text);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}