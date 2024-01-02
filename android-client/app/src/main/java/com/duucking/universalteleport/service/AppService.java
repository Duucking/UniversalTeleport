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
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.service.quicksettings.TileService;

import androidx.annotation.NonNull;

import com.duucking.universalteleport.BuildConfig;
import com.duucking.universalteleport.R;
import com.duucking.universalteleport.util.TeleportUtil;
import com.duucking.universalteleport.util.ClipboardUtil;

import java.io.IOException;
import java.net.ServerSocket;

public class AppService extends Service {
    private final String CHANNEL_DEFAULT_IMPORTANCE = "8848";
    private final int ONGOING_NOTIFICATION_ID = 114;
    private ServerSocket message_server_socket;
    private ServerSocket file_server_socket;
    private Thread getMessagethread;
    private Thread getFilethread;
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Log.e("UniversalTeleportTest", "handle处理消息" + msg.obj);
                try {
                    if (msg.obj.equals("funtion:fileTrans")) {
                        getFilethread = new Thread(new getFileThread());
                        getFilethread.start();
                    } else {
                        ClipboardUtil.setClipboard(getBaseContext(), String.valueOf(msg.obj));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
            message_server_socket = new ServerSocket(8556);
            file_server_socket = new ServerSocket(8558, 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Service init
        createNotification();
        //Create thread to listen tcp message
        getMessagethread = new Thread(new getTCPThread());
        getMessagethread.start();
        TileService.requestListeningState(this, new ComponentName(BuildConfig.APPLICATION_ID, QuickActionService.class.getName()));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            message_server_socket.close();
            file_server_socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TileService.requestListeningState(this, new ComponentName(BuildConfig.APPLICATION_ID, QuickActionService.class.getName()));
        Log.e("UniversalTeleportTest", "AppService destroy");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel_id);
            String description = getString(R.string.notification_channel_desc);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel FGS_channel = new NotificationChannel(CHANNEL_DEFAULT_IMPORTANCE, name, importance);
            FGS_channel.setDescription(description);
            NotificationChannel FUS_channel = new NotificationChannel("8849", "文件传输通知", NotificationManager.IMPORTANCE_HIGH);
            FUS_channel.setDescription("文件传输时会发出此通知");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(FGS_channel);
            notificationManager.createNotificationChannel(FUS_channel);
        }
    }

    private void createNotification() {
        Intent notificationIntent = new Intent(this, R.class);
        createNotificationChannel();
        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, ONGOING_NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, ONGOING_NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        }

        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Log.e("AppService", "buildNotification");
            notification = new Notification.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
                    .setContentTitle(getString(R.string.notification_title_name))
                    .setContentText(getString(R.string.notification_title_content1))
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
//                        .setTicker(getText(R.string.ticker_text))
                    .build();
        }
        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    public class getTCPThread implements Runnable {
        @Override
        public void run() {
            try {
                TeleportUtil.getTCPMessage(handler, message_server_socket);
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
                        .setContentTitle("文件传送")
                        .setContentText("传送失败咧~")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setAutoCancel(true);
                notificationManager.cancel(515);
                notificationManager.notify(515, notificationbuilder.build());
                e.printStackTrace();
            }
        }
    }
}