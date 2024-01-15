package com.duucking.universalteleport;

import static com.duucking.universalteleport.util.DensityUtil.dp2pix;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.duucking.universalteleport.model.Device;
import com.duucking.universalteleport.util.TeleportUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


public class ShareActivity extends AppCompatActivity implements View.OnClickListener {
    private ServerSocket discover_server_socket;
    private final ArrayList<Device> deviceList = new ArrayList<>();
    private Thread sendFileThread;
    private Uri uri;
    private LinearLayout loadingLayout;
    private LinearLayout deviceListLayout;
    private boolean isSendDeviceDiscover;


    private final Handler shareHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Log.e("UniversalTeleportTest", "handle处理设备消息");
                    JSONObject deviceObject = JSONObject.parseObject(msg.obj.toString());
                    String IP = deviceObject.getString("IP");
                    String deviceName = deviceObject.getString("deviceName");
                    String Key = deviceObject.getString("Key");
                    String deviceType = deviceObject.getString("deviceType");
                    Device device = new Device(IP, deviceName, Key, deviceType);
                    SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
                    String myKey = sharedPreferences.getString("key", "");
                    if (!myKey.equals("")) {
                        try {
                            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                            byte[] encryptKey = messageDigest.digest(myKey.getBytes(StandardCharsets.UTF_8));
                            StringBuilder stringBuilder = new StringBuilder();
                            for (byte b : encryptKey) {
                                stringBuilder.append(String.format("%02x", b));
                            }
                            myKey = stringBuilder.toString();
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                    }
                    if (myKey.equals(Key)) {
                        deviceList.add(device);
                        refreshDeviceList();
                    }
                    break;
                case 2:
                    Log.e("UniversalTeleportTest", "handle处理设备消息2");
                    finish();
                    break;
                case 3:
                    deviceListLayout.setVisibility(View.GONE);
                    loadingLayout.setVisibility(View.VISIBLE);
            }
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        loadingLayout = findViewById(R.id.loadingLayout);
        deviceListLayout = findViewById(R.id.deviceListLayout);
        isSendDeviceDiscover = true;
        try {
            discover_server_socket = new ServerSocket(8559, 5);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Thread deviceDiscoverThread = new Thread(new deviceDiscover(shareHandler, discover_server_socket));
        deviceDiscoverThread.start();
        Thread sendDeviceDiscoverThread = new Thread(new sendDeviceDiscoverMsg());
        sendDeviceDiscoverThread.start();

        findViewById(R.id.area1).setOnClickListener(this);
        findViewById(R.id.area2).setOnClickListener(this);
        findViewById(R.id.area3).setOnClickListener(this);
        findViewById(R.id.area4).setOnClickListener(this);
        findViewById(R.id.area5).setOnClickListener(this);
        findViewById(R.id.area6).setOnClickListener(this);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action) && extras != null) {
            if (extras.containsKey(Intent.EXTRA_STREAM)) {
                try {
                    uri = extras.getParcelable(Intent.EXTRA_STREAM);
                    Log.e("UniversalTeleportTest", "uri:" + uri);
                    SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
                    String ip = sharedPreferences.getString("ip", "");
                    if (!ip.equals("")) {
                        sendFileThread = new Thread(new sendFileThread(shareHandler, ip));
                        sendFileThread.start();
                        moveTaskToBack(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isSendDeviceDiscover = false;
        try {
            discover_server_socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("UniversalTeleportTest", "share activity destroy");
    }

    @SuppressLint("ResourceType")
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
        TextView deviceIP = findViewById((v.getId() + 3));
        sendFileThread = new Thread(new sendFileThread(shareHandler, deviceIP.getText().toString()));
        sendFileThread.start();
        Toast.makeText(this, getString(R.string.share_activity_start_share_toast_text), Toast.LENGTH_SHORT).show();
        moveTaskToBack(true);
    }

    public void refreshDeviceList() {
        float scale = getResources().getDisplayMetrics().density;
        deviceListLayout.removeAllViewsInLayout();
        if (!deviceList.isEmpty()) {
            loadingLayout.setVisibility(View.GONE);
            for (int i = 0; i < deviceList.size(); i++) {
                Device d = deviceList.get(i);
                LinearLayout deviceLayout = new LinearLayout(this);
                int layoutID = (i + 1) * 100 + 1;
                deviceLayout.setId(layoutID);
                deviceLayout.setOrientation(LinearLayout.HORIZONTAL);
                ImageView deviceIcon = new ImageView(this);
                int iconID = (i + 1) * 100 + 2;
                deviceIcon.setId(iconID);
                boolean darkModeActive = (getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_YES) != 0;
                int phoneDrawable = R.drawable.ic_devices_phone;
                int pcDrawable = R.drawable.ic_device_pc;
                if (darkModeActive) {
                    phoneDrawable = R.drawable.ic_devices_phone_darkmode;
                    pcDrawable = R.drawable.ic_device_pc_darkmode;
                }
                if (d.getDeviceType().equals("Android")) {
                    deviceIcon.setImageResource(phoneDrawable);
                } else {
                    deviceIcon.setImageResource(pcDrawable);
                }
                TextView deviceName = new TextView(this);
                int nameID = (i + 1) * 100 + 3;
                deviceName.setGravity(Gravity.CENTER | Gravity.START);
                deviceName.setId(nameID);
                deviceName.setText(d.getDeviceName());
                deviceName.setTextSize(18);
                TextView IP = new TextView(this);
                int ipID = (i + 1) * 100 + 4;
                IP.setId(ipID);
                IP.setText(d.getIP());
                IP.setTextSize(14);
                IP.setTextColor(Color.DKGRAY);
                IP.setGravity(Gravity.CENTER | Gravity.START);
                LinearLayout deviceInfoLayout = new LinearLayout(this);
                deviceInfoLayout.setOrientation(LinearLayout.VERTICAL);
                deviceInfoLayout.addView(deviceName, new LinearLayout.LayoutParams(dp2pix(scale, 265), LinearLayout.LayoutParams.WRAP_CONTENT));
                deviceInfoLayout.addView(IP, new LinearLayout.LayoutParams(dp2pix(scale, 265), LinearLayout.LayoutParams.WRAP_CONTENT));
                deviceInfoLayout.setGravity(Gravity.CENTER | Gravity.START);
                deviceLayout.addView(deviceIcon, new LinearLayout.LayoutParams(dp2pix(scale, 65), dp2pix(scale, 45)));
                deviceLayout.addView(deviceInfoLayout, new LinearLayout.LayoutParams(dp2pix(scale, 265), LinearLayout.LayoutParams.MATCH_PARENT));
                deviceLayout.setOnClickListener(this);
                deviceLayout.setClickable(true);
                TypedValue typedValue = new TypedValue();
                getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
                int[] attribute = new int[]{android.R.attr.selectableItemBackground};
                TypedArray typedArray = getTheme().obtainStyledAttributes(typedValue.resourceId, attribute);
                deviceLayout.setBackground(typedArray.getDrawable(0));

                deviceListLayout.addView(deviceLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                deviceListLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    public class sendFileThread implements Runnable {
        private final String IP;
        private final Handler handler;

        public sendFileThread(Handler handler, String IP) {
            this.IP = IP;
            this.handler = handler;
        }

        @Override
        public void run() {
            try {
                SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
                String address = sharedPreferences.getString("ip", "");
                if (address.equals("")) {
                    Log.e("UniversalTeleportTest", "send to " + IP);
                    TeleportUtil.sendTCPMessage(IP, 8556, "funtion:fileTrans");
                    TeleportUtil.sendFile(handler, getBaseContext(), IP, 8558, uri);
                } else {
                    TeleportUtil.sendTCPMessage(address, 8556, "funtion:fileTrans");
                    TeleportUtil.sendFile(handler, getBaseContext(), address, 8558, uri);
                }

            } catch (IOException e) {
                TeleportUtil.setTransStateFalse("send");
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                Notification.Builder notificationbuilder = new Notification.Builder(getBaseContext(), "8849")
                        .setContentTitle(getString(R.string.notification_file_share_title))
                        .setContentText(getString(R.string.notification_file_share_result_fail_send))
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setAutoCancel(true);
                notificationManager.cancel(514);
                notificationManager.notify(514, notificationbuilder.build());
                e.printStackTrace();
            }
        }
    }

    public class deviceDiscover implements Runnable {
        private final Handler handler;
        private final ServerSocket serverSocket;

        public deviceDiscover(Handler handler, ServerSocket serverSocket) {
            this.handler = handler;
            this.serverSocket = serverSocket;

        }

        @Override
        public void run() {
            try {
                TeleportUtil.listenTCPMessage(handler, serverSocket);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    discover_server_socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public class sendDeviceDiscoverMsg implements Runnable {

        @Override
        public void run() {
            try {
                while (isSendDeviceDiscover) {
                    TeleportUtil.sendUDPMessage(8557, "funtion:deviceDiscover");
                    Thread.sleep(5000);
                    deviceList.clear();
                    Message msg = Message.obtain();
                    msg.what = 3;
                    shareHandler.sendMessage(msg);
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}