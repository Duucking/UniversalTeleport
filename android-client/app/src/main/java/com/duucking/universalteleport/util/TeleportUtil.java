package com.duucking.universalteleport.util;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;


public class TeleportUtil {
    private static boolean isOnRecv = false;
    private static boolean isOnSend = false;
    private static int recvProgress = 0;
    private static int sendProgress = 0;

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
        os.close();
        socket.close();
    }

    private static String readMessageFromStream(Socket socket) throws IOException {
        InputStreamReader isr = new InputStreamReader(socket.getInputStream());
        BufferedReader reader = new BufferedReader(isr);
        char[] chars = new char[1024];
        int byteread;
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
    public static void listenTCPMessage(Handler handler, ServerSocket serverSocket) throws IOException {
        while (true) {
            Socket socket = serverSocket.accept();
            String ipAddr = socket.getInetAddress().getHostAddress();
            Log.e("UniversalTeleportTest", "连接成功，IP:" + ipAddr);
            String message = readMessageFromStream(socket);
            Log.e("UniversalTeleportTest", "getMessage:" + message);
            socket.shutdownInput();
            Log.e("UniversalTeleportTest", "Connection closed");
            Message msg = Message.obtain();
            msg.what = 1;
            msg.obj = new String[]{message, ipAddr};
            handler.sendMessage(msg);
        }
    }


    @SuppressLint("Range")
    public static void sendFile(Handler handler, Context context, String address, int port, Uri uri) throws IOException {
        InputStream input = context.getContentResolver().openInputStream(uri);
        long fileSize = 0;
        String fileName = "";
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            if (!cursor.isNull(sizeIndex)) {
                fileSize = cursor.getLong(sizeIndex);
            }
            cursor.close();
        }
        Log.e("UniversalTeleportTest", "filesize: " + fileSize);
        Log.e("UniversalTeleportTest", "filename: " + fileName);
        BufferedInputStream bis = new BufferedInputStream(input);
        Socket socket = new Socket(address, port);
        OutputStream os = socket.getOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(os);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String message;
        byte[] tempbytes = new byte[20480];
        int byteread;
        long bytetrans = 0;
        float progress;
        os.write(String.valueOf(fileSize).getBytes());
        os.write("\n".getBytes());
        os.write(fileName.getBytes());
        os.write("\n".getBytes());
        message = reader.readLine();
        if (message.equals("received")) {
            Log.e("UniversalTeleportTest", "getMessage:" + message);
        }
        isOnSend = true;
        Thread notifyProgressThread = new Thread(new notifyTransProgress(context, fileName, "send"));
        notifyProgressThread.start();
        Message msg = Message.obtain();
        msg.what = 2;
        handler.sendMessage(msg);
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
        }
        socket.shutdownInput();
        socket.shutdownOutput();
        socket.close();
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
        os.write("received\n".getBytes());
        String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/UniversalTeleport";
        File downloadPathFile = new File(downloadPath);
        if (!downloadPathFile.exists()) {
            if (!downloadPathFile.mkdir()) {
                throw new IOException("create dir failed");
            }
        }
        File downloadFile = new File(downloadPath + "/" + fileName);
        if (downloadFile.exists()) {
            int nameEnd = fileName.lastIndexOf('.');
            StringBuilder stringBuilder = new StringBuilder(fileName);
            stringBuilder.insert(nameEnd, System.currentTimeMillis());
            fileName = stringBuilder.toString();
            downloadFile = new File(downloadPath + "/" + fileName);
        }
        Log.e("UniversalTeleportTest", "downloadPath: " + downloadPath + "/" + fileName);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(downloadPath + "/" + fileName));
        long bytetrans = 0;
        int byteread;
        float progress;
        byte[] tempbytes = new byte[20480];
        isOnRecv = true;
        Thread notifyProgressThread = new Thread(new notifyTransProgress(context, fileName, "recv"));
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
        if (bytetrans < fileSize) {
            if (!downloadFile.delete()) {
                Log.e("UniversalTeleportTest", "delete file failed: " + downloadPath + "/" + fileName);
            }
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".jpeg")
                || fileName.endsWith(".gif") || fileName.endsWith(".mp4") || fileName.endsWith(".mkv")
                || fileName.endsWith(".mov") || fileName.endsWith(".wmv")) {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), BitmapFactory.decodeFile(downloadFile.getAbsolutePath()), downloadFile.getName(), null);
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(downloadFile);
            intent.setData(uri);
            context.sendBroadcast(intent);
        }
        os.write("finish\n".getBytes());
        socket.shutdownInput();
        socket.shutdownOutput();
    }

    public static void sendUDPMessage(int port, String msg) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket();
        DatagramPacket datagramPacket;
        byte[] data = msg.getBytes();
        String broadcastAddress = getBroadcast();
        if (broadcastAddress != null) {
            Log.e("UniversalTeleportTest", "sendData: " + msg);
            datagramPacket = new DatagramPacket(data, 0, data.length, new InetSocketAddress(broadcastAddress, port));
            datagramSocket.send(datagramPacket);
        }
        datagramSocket.close();
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public static void listenUDPMessage(Handler handler, DatagramSocket datagramSocket) throws IOException {
        byte[] data = new byte[10240];
        DatagramPacket datagramPacket = new DatagramPacket(data, 0, data.length);
        while (true) {
            Log.e("UniversalTeleportTest", "start listen");
            datagramSocket.receive(datagramPacket);
            data = datagramPacket.getData();
            int dataLength = datagramPacket.getLength();
            String ip = datagramPacket.getAddress().toString();
            if (!ip.equals(getIpAddress())) {
                String message = new String(data, 0, dataLength);
                Log.e("UniversalTeleportTest", "dataLength: " + dataLength);
                Message msg = Message.obtain();
                if (message.equals("funtion:deviceDiscover")) {
                    msg.what = 2;
                    msg.obj = ip;
                } else {
                    msg.what = 1;
                    msg.obj = new String[]{message, ip};
                }
                handler.sendMessage(msg);
            }
            datagramSocket.disconnect();
        }
    }

    private static class notifyTransProgress implements Runnable {
        private final Context context;
        private final String fileName;
        private final String transType;

        public notifyTransProgress(Context context, String fileName, String transType) {
            this.context = context;
            this.fileName = fileName;
            this.transType = transType;
        }

        @Override
        public void run() {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification.Builder notificationbuilder = new Notification.Builder(context, "8849")
                    .setContentTitle(context.getString(R.string.notification_file_share_title))
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setProgress(100, 0, false)
                    .setAutoCancel(true);
            if (transType.equals("send")) {
                while (isOnSend) {
                    notificationbuilder.setContentText(fileName + context.getString(R.string.notification_file_share_type_send) + sendProgress + "%");
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
                    notificationbuilder.setContentText(fileName + context.getString(R.string.notification_file_share_result_success_send));
                    notificationManager.notify(514, notificationbuilder.build());
                    sendProgress = 0;
                }
            }
            if (transType.equals("recv")) {
                while (isOnRecv) {
                    notificationbuilder.setContentText(fileName + context.getString(R.string.notification_file_share_type_recv) + recvProgress + "%");
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
                    notificationbuilder.setContentText(fileName + context.getString(R.string.notification_file_share_result_success_recv));
                    notificationManager.notify(515, notificationbuilder.build());
                    recvProgress = 0;
                } else {
                    notificationbuilder.setProgress(0, 0, false);
                    notificationbuilder.setContentText(fileName + context.getString(R.string.notification_file_share_result_fail_recv));
                    notificationManager.notify(515, notificationbuilder.build());
                    recvProgress = 0;
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


    public static String getBroadcast() throws SocketException {
        System.setProperty("java.net.preferIPv4Stack", "true");
        for (Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces(); niEnum.hasMoreElements(); ) {
            NetworkInterface ni = niEnum.nextElement();
            if (!ni.isLoopback()) {
                for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses()) {
                    if (interfaceAddress.getBroadcast() != null) {
                        return interfaceAddress.getBroadcast().toString().substring(1);
                    }
                }
            }
        }
        return null;
    }

    public static String getIpAddress() throws SocketException {
        System.setProperty("java.net.preferIPv4Stack", "true");
        for (Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces(); niEnum.hasMoreElements(); ) {
            NetworkInterface ni = niEnum.nextElement();
            if (!ni.isLoopback()) {
                for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses()) {
                    if (interfaceAddress.getBroadcast() != null) {
                        return interfaceAddress.getAddress().toString();
                    }
                }
            }
        }
        return null;
    }

}