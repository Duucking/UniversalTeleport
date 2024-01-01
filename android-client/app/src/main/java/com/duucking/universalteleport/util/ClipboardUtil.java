package com.duucking.universalteleport.util;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;

public class ClipboardUtil {
    /**
     * Set a text to clipboard
     *
     * @param context Context
     * @param text    Content
     */
    public static void setClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("FromUniversalTeleport", text);
        Log.e("UniversalTeleportTest", "复制到剪切板成功");
        if (clipboard != null) {
            clipboard.setPrimaryClip(mClipData);
        }
    }

    public static String getClipboard() {
        return "true";
    }
}
