package com.duucking.universalteleport.service;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Icon;
import android.os.IBinder;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import com.duucking.universalteleport.MainActivity;
import com.duucking.universalteleport.R;
import com.duucking.universalteleport.ShareActivity;
import com.duucking.universalteleport.ShareTextActivity;
import com.duucking.universalteleport.noActivity;

import java.util.List;

public class QuickActionService extends TileService {
    public QuickActionService() {
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Tile tile = getQsTile();
        tile.setState(Tile.STATE_INACTIVE);
        tile.setIcon(Icon.createWithResource(this, R.drawable.ic_public_email_send_filled));
        tile.updateTile();
        Log.e("UniversalTeleportTest", "tile service onTaskRemoved");
        super.onTaskRemoved(rootIntent);

    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Log.e("UniversalTeleportTest", "tile service Listening");
        changeTileState();

    }

    @Override
    public void onClick() {
        super.onClick();
        Tile tile = getQsTile();
        if (tile.getState() != Tile.STATE_ACTIVE) {
            Intent noActivityinten = new Intent(this, noActivity.class);
            noActivityinten.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                startActivity(noActivityinten);
            } catch (Exception e) {
                Log.e("UniversalTeleportTest", "Exception :" + e);
            }

            tile.setState(Tile.STATE_ACTIVE);
            tile.setIcon(Icon.createWithResource(this, R.drawable.ic_public_email_send));
            tile.updateTile();
        } else {
            Intent AppServiceintent = new Intent(this, AppService.class);
            tile.setState(Tile.STATE_INACTIVE);
            tile.setIcon(Icon.createWithResource(this, R.drawable.ic_public_email_send_filled));
            tile.updateTile();
            MainActivity mainActivity = new MainActivity();
            noActivity noActivity = new noActivity();
            ShareActivity shareActivity = new ShareActivity();
            ShareTextActivity shareTextActivity = new ShareTextActivity();
            mainActivity.finish();
            noActivity.finish();
            shareActivity.finish();
            shareTextActivity.finish();
            stopService(AppServiceintent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        changeTileState();
        Log.e("UniversalTeleportTest", "tile service destroy");
    }

    private void changeTileState() {
        Tile tile = getQsTile();
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo service : runningServices) {
            if ("com.duucking.universalteleport.service.AppService".equals(service.service.getClassName())) {
                // MyService 已经启动
                Log.e("UniversalTeleportTest", "AppService is running");
                tile.setState(Tile.STATE_ACTIVE);
                tile.setIcon(Icon.createWithResource(this, R.drawable.ic_public_email_send));
                tile.updateTile();
                break;
            } else {
                Log.e("UniversalTeleportTest", "AppService is not running");
                tile.setState(Tile.STATE_INACTIVE);
                tile.setIcon(Icon.createWithResource(this, R.drawable.ic_public_email_send_filled));
                tile.updateTile();
            }
        }
    }

}