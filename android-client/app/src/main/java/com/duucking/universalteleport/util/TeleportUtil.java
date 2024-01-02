package com.duucking.universalteleport.util;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.OpenableColumns;
import android.util.Log;

import com.duucking.universalteleport.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class TeleportUtil {
    private static boolean isOnRecv = false;
    private static boolean isOnSend = false;
    private static int recvProgress = 0;
    private static int sendProgress = 0;


    private static String readMessageFromStream(Socket socket) throws IOException {
        InputStreamReader isr = new InputStreamReader(socket.getInputStream());
        BufferedReader reader = new BufferedReader(isr);
        char[] chars = new char[1024];
        int byteread = 0;
        StringBuilder stringBuilder = new StringBuilder();
        while ((byteread = reader.read(chars)) != -1) {
            stringBuilder.append(chars, 0, byteread);
        }
        return stringBuilder.toString();
    }

    /**
     * Create a server to listen tcp message
     *
     * @param handler Handler
     */
    public static void getTCPMessage(Handler handler, ServerSocket serverSocket) throws IOException {
//        ServerSocket serverSocket = new ServerSocket(8556);
        try {
            Log.e("UniversalTeleportTest", "创建Socket成功，等待连接...");
            while (true) {
                Socket socket = serverSocket.accept();
                Log.e("UniversalTeleportTest", "连接成功，IP:" + socket.getInetAddress().getHostAddress());
                String message = readMessageFromStream(socket);
                Log.e("UniversalTeleportTest", "getMessage:" + message);
                socket.shutdownInput();
                Log.e("UniversalTeleportTest", "Connection closed");
                if (socket.getInetAddress().getHostAddress().equals("127.0.0.1") && message.equals("Close")) {
                    break;
                }
                socket.close();
                Message msg = Message.obtain();
                msg.what = 1;
                msg.obj = message;
                handler.sendMessage(msg);
            }
            Log.e("UniversalTeleportTest", "Socket closed");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Send message by TCP
     *
     * @param address Internet Protocol
     * @param port    Port
     * @param msg     Message
     */
    public static void sendTCPMessage(String address, int port, String msg) throws IOException {
        Socket socket = new Socket(address, port);
        OutputStream os = socket.getOutputStream();
        os.write(msg.getBytes());
        socket.shutdownOutput();
        os.close();
    }

    @SuppressLint("Range")
    public static void sendFile(Context context, String address, int port, Uri uri) throws IOException {
        InputStream input = context.getContentResolver().openInputStream(uri);
        long fileSize = input.available();
        String fileName = "";
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            cursor.close();
        }
        Log.e("UniversalTeleportTest", "filesize: " + fileSize);
        Log.e("UniversalTeleportTest", "filename: " + fileName);
        BufferedInputStream bis = new BufferedInputStream(input);
        Socket socket = new Socket(address, port);
        OutputStream os = socket.getOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(os);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String message = "";
        byte[] tempbytes = new byte[20480];
        int byteread = 0;
        long bytetrans = 0;
        float progress = 0;
        os.write(String.valueOf(fileSize).getBytes());
        os.write("\n".getBytes());
        os.write(fileName.getBytes());
        os.write("\n".getBytes());
        message = reader.readLine();
        if (message.equals("received")) {
            Log.e("UniversalTeleportTest", "getMessage:" + message);
        }
        isOnSend = true;
        Thread notifyProgressThread = new Thread(new notifyTransProgress(context, fileName, "send", socket));
        notifyProgressThread.start();
        while ((byteread = bis.read(tempbytes)) != -1) {
            bytetrans += byteread;
            progress = (float) bytetrans / fileSize * 100;
            sendProgress = (int) progress;
            bos.write(tempbytes, 0, byteread);
        }
        isOnSend = false;
        bos.flush();
        message = reader.readLine();
        Log.e("UniversalTeleportTest", "getMessage:" + message);
        if (message.equals("finish")) {
            Log.e("UniversalTeleportTest", "send end");
            socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();
        }
    }

    public static void receiveFile(Context context, ServerSocket serverSocket) throws IOException {
        Log.e("UniversalTeleportTest", "start receive file ");
        Socket socket = serverSocket.accept();
        OutputStream os = socket.getOutputStream();
        InputStream is = socket.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        long fileSize = Long.parseLong(reader.readLine());
        String fileName = reader.readLine();
        Log.e("UniversalTeleportTest", "filesize: " + fileSize);
        Log.e("UniversalTeleportTest", "filename: " + fileName);
        os.write("received\n".getBytes());
        String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/UniversalTeleport";
        File file = new File(downloadPath);
        if (!file.exists()) {
            file.mkdir();
        }
        Log.e("UniversalTeleportTest", "downloadPath: " + downloadPath + "/" + fileName);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(downloadPath + "/" + fileName));
//        FileOutputStream fos = new FileOutputStream(downloadPath + "/" + fileName);
        long bytetrans = 0;
        int byteread = 0;
        float progress = 0;
        byte[] tempbytes = new byte[20480];
        isOnRecv = true;
        Thread notifyProgressThread = new Thread(new notifyTransProgress(context, fileName, "recv", socket));
        notifyProgressThread.start();
        while ((byteread = bis.read(tempbytes)) != -1) {
            bytetrans += byteread;
            progress = (float) bytetrans / fileSize * 100;
            recvProgress = (int) progress;
            if (bytetrans == fileSize) {
                bos.write(tempbytes, 0, byteread);
                break;
            }
            bos.write(tempbytes, 0, byteread);
        }
        isOnRecv = false;
        bos.flush();
        bos.close();
        if (bytetrans<fileSize){
            file = new File(downloadPath + "/" + fileName);
            file.delete();
        }
        os.write("finish\n".getBytes());
        // 释放资源
        socket.shutdownInput();
        socket.shutdownOutput();
        Log.e("UniversalTeleportTest", "trans finish ");
    }

    private static class notifyTransProgress implements Runnable {
        private Context context;
        private String fileName;
        private String transType;
        private Socket socket;

        public notifyTransProgress(Context context, String fileName, String transType, Socket socket) {
            this.context = context;
            this.fileName = fileName;
            this.transType = transType;
            this.socket = socket;
        }

        @Override
        public void run() {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification.Builder notificationbuilder = new Notification.Builder(context, "8849")
                    .setContentTitle("文件传送")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setProgress(100, 0, false)
                    .setAutoCancel(true);
            if (transType.equals("send")) {
                while (isOnSend && !socket.isClosed()) {
                    notificationbuilder.setContentText(fileName + "已发送" + sendProgress + "%");
                    notificationbuilder.setProgress(100, sendProgress, false);
                    notificationManager.notify(514, notificationbuilder.build());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (sendProgress == 100) {
                    notificationbuilder.setProgress(0, 0, false);
                    notificationbuilder.setContentText(fileName + "发送完成");
                    notificationManager.notify(514, notificationbuilder.build());
                }
            }
            if (transType.equals("recv")) {
                while (isOnRecv && !socket.isClosed()) {
                    notificationbuilder.setContentText(fileName + "已接收" + recvProgress + "%");
                    notificationbuilder.setProgress(100, recvProgress, false);
                    notificationManager.notify(515, notificationbuilder.build());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (recvProgress == 100) {
                    notificationbuilder.setProgress(0, 0, false);
                    notificationbuilder.setContentText(fileName + "接收完成");
                    notificationManager.notify(515, notificationbuilder.build());
                } else {
                    notificationbuilder.setProgress(0, 0, false);
                    notificationbuilder.setContentText(fileName + "接收失败");
                    notificationManager.notify(515, notificationbuilder.build());
                }
            }

        }
    }

    public static void setTransStateFalse(String transType) {
        switch (transType) {
            case "send":
                isOnSend = false;
                break;
            case "recv":
                isOnRecv = false;
                break;
            default:
                break;
        }
    }

}