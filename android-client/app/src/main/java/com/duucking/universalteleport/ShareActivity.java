package com.duucking.universalteleport;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.duucking.universalteleport.service.AppService;
import com.duucking.universalteleport.util.TeleportUtil;

import java.io.IOException;


public class ShareActivity extends AppCompatActivity implements View.OnClickListener {

    private final Thread sendMessagethread = new Thread(new sendTCPThread());
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        findViewById(R.id.area1).setOnClickListener(this);
        findViewById(R.id.area2).setOnClickListener(this);
        findViewById(R.id.area3).setOnClickListener(this);
        findViewById(R.id.area4).setOnClickListener(this);
        findViewById(R.id.area5).setOnClickListener(this);
        findViewById(R.id.area6).setOnClickListener(this);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            if (extras.containsKey(Intent.EXTRA_STREAM)) {
                try {
                    uri = extras.getParcelable(Intent.EXTRA_STREAM);
                    Log.e("UniversalTeleportTest", "uri:" + uri);
//                    TeleportUtil.sendFile(this, "192.168.100.3", 8556, uri);
                    sendMessagethread.start();

                } catch (Exception e) {
                    Log.e(this.getClass().getName(), e.toString());
                }

            } else if (extras.containsKey(Intent.EXTRA_TEXT)) {
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("UniversalTeleportTest", "dialog destroy");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.area1 || v.getId() == R.id.area3 || v.getId() == R.id.area4) {
            Toast.makeText(this, "发送成功！", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (v.getId() == R.id.area2) {
            Toast.makeText(this, "发送失败！", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (v.getId() == R.id.area5) {
            Toast.makeText(this, "500 Internal Error！", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (v.getId() == R.id.area6) {
            Toast.makeText(this, "ᕕ(◠ڼ◠)ᕗ", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    public class sendTCPThread implements Runnable {
        @Override
        public void run() {
            try {
                SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
                String address = sharedPreferences.getString("ip", "");
                TeleportUtil.sendTCPMessage(address, 8556, "funtion:fileTrans");
                TeleportUtil.sendFile(getBaseContext(), address, 8558, uri);
            } catch (IOException e) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                Notification.Builder notificationbuilder = new Notification.Builder(getBaseContext(), "8849")
                        .setContentTitle("文件传送")
                        .setContentText("传送失败咧~")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setAutoCancel(true);
                notificationManager.cancel(514);
                notificationManager.notify(514, notificationbuilder.build());
            }
        }
    }
}