package com.duucking.universalteleport.util;

public class DensityUtil {
    public static int pix2dp(float scale, int pix) {
        return (int) (pix / scale + 0.5f);
    }

    public static int dp2pix(float scale, int dp) {
        return (int) ((dp - 0.5f) * scale);
    }
}
