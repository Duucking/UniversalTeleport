package com.duucking.universalteleport.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.service.quicksettings.TileService;

import androidx.annotation.NonNull;


import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.duucking.universalteleport.BuildConfig;
import com.duucking.universalteleport.R;
import com.duucking.universalteleport.util.DataUtil;
import com.duucking.universalteleport.util.TeleportUtil;
import com.duucking.universalteleport.util.ClipboardUtil;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class AppService extends Service {
    private final String CHANNEL_DEFAULT_IMPORTANCE = "8848";
    private ServerSocket message_server_socket;
    private ServerSocket file_server_socket;
    private DatagramSocket message_datagram_socket;

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    try {
                        if (msg.obj.equals("funtion:fileTrans")) {
                            Thread getFilethread = new Thread(new getFileThread());
                            getFilethread.start();
                        } else {
                            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
                            String key = sharedPreferences.getString("key", "");
                            String data = DataUtil.Decrypt(String.valueOf(msg.obj), key);
                            if (data == null) {
                                data = String.valueOf(msg.obj);
                            }
                            ClipboardUtil.setClipboard(getBaseContext(), data);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
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
                    String targetIP = msg.obj.toString().replace("/", "");
                    String deviceName = sharedPreferences.getString("deviceName", "");
                    String deviceIP = "";
                    String key = sharedPreferences.getString("key", "");
                    try {
                        deviceIP = TeleportUtil.getIpAddress();
                        if (deviceIP != null) {
                            deviceIP = deviceIP.replace("/", "");
                        }
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                    if (!key.equals("")) {
                        try {
                            MessageDigest object = MessageDigest.getInstance("SHA-256");
                            byte[] encryptKey = object.digest(key.getBytes(StandardCharsets.UTF_8));
                            StringBuilder stringBuilder = new StringBuilder();
                            for (byte b : encryptKey) {
                                stringBuilder.append(Integer.toHexString(0xff & b));
                            }
                            key = stringBuilder.toString();
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                    }
                    JSONObject deviceObject = new JSONObject();
                    try {
                        deviceObject.put("deviceType", "Android");
                        deviceObject.put("deviceName", deviceName);
                        deviceObject.put("IP", deviceIP);
                        deviceObject.put("Key", key);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Thread sendTCPMessagethread = new Thread(new sendTCPThread(targetIP, 8559, deviceObject.toString()));
                    sendTCPMessagethread.start();
                    break;
            }
        }
    };

    public AppService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("UniversalTeleportTest", "AppService init");
        try {
            message_server_socket = new ServerSocket(8556, 5);
            file_server_socket = new ServerSocket(8558, 1);
            message_datagram_socket = new DatagramSocket(8557);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //Service init
        createNotification();
        //Create thread to listen tcp message
        Thread getTCPMessagethread = new Thread(new getTCPThread());
        getTCPMessagethread.start();
        Thread getUDPMessagethread = new Thread(new getUDPThread());
        getUDPMessagethread.start();
        TileService.requestListeningState(this, new ComponentName(BuildConfig.APPLICATION_ID, QuickActionService.class.getName()));
    }

    @Override
    public void onDestroy() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        try {
            message_server_socket.close();
            file_server_socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TileService.requestListeningState(this, new ComponentName(BuildConfig.APPLICATION_ID, QuickActionService.class.getName()));
        Log.e("UniversalTeleportTest", "AppService destroy");
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    private void createNotificationChannel() {
        CharSequence name = getString(R.string.notification_main_channel_name);
        String description = getString(R.string.notification_main_channel_desc);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel FGS_channel = new NotificationChannel(CHANNEL_DEFAULT_IMPORTANCE, name, importance);
        FGS_channel.setDescription(description);
        NotificationChannel FUS_channel = new NotificationChannel("8849", getString(R.string.notification_file_channel_name), NotificationManager.IMPORTANCE_HIGH);
        FUS_channel.setDescription(getString(R.string.notification_file_channel_desc));
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(FGS_channel);
        notificationManager.createNotificationChannel(FUS_channel);

    }

    private void createNotification() {
        Intent notificationIntent = new Intent(this, R.class);
        createNotificationChannel();
        PendingIntent pendingIntent;
        int ONGOING_NOTIFICATION_ID = 114;
        pendingIntent = PendingIntent.getActivity(this, ONGOING_NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new Notification.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
                .setContentTitle(getString(R.string.notification_title_name))
                .setContentText(getString(R.string.notification_title_content1))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);
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


    public class getTCPThread implements Runnable {
        @Override
        public void run() {
            try {
                Log.e("UniversalTeleportTest", "listen tcp thread start");
                TeleportUtil.listenTCPMessage(handler, message_server_socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class getUDPThread implements Runnable {
        @Override
        public void run() {
            try {
                Log.e("UniversalTeleportTest", "listen udp thread start");
                TeleportUtil.listenUDPMessage(handler, message_datagram_socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public class getFileThread implements Runnable {
        @Override
        public void run() {
            try {
                TeleportUtil.receiveFile(getBaseContext(), file_server_socket);
            } catch (IOException e) {
                TeleportUtil.setTransStateFalse("recv");
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                Notification.Builder notificationbuilder = new Notification.Builder(getBaseContext(), "8849")
                        .setContentTitle(getString(R.string.notification_file_share_title))
                        .setContentText(getString(R.string.notification_file_share_result_fail_recv))
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setAutoCancel(true);
                notificationManager.cancel(515);
                notificationManager.notify(515, notificationbuilder.build());
                e.printStackTrace();
            }
        }
    }
}