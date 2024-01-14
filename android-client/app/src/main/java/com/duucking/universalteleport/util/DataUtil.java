package com.duucking.universalteleport.util;

import static android.text.TextUtils.isEmpty;

import android.util.Log;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DataUtil {
    public static String Encrypt(String sSrc, String sKey) throws Exception {
        if (isEmpty(sSrc) || isEmpty(sKey)) {
            return null;
        }
        sKey = to16ByteMD5(sKey);
        String iv = to16ByteMD5("114514");
        Log.e("UniversalTeleportTest", "start encrypt");
        byte[] raw = sKey.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(iv.getBytes()));
        byte[] encrypted = cipher.doFinal(sSrc.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String Decrypt(String sSrc, String sKey) throws Exception {
        if (isEmpty(sSrc) || isEmpty(sKey)) {
            return null;
        }
        sKey = to16ByteMD5(sKey);
        String iv = to16ByteMD5("114514");
        Log.e("UniversalTeleportTest", "start decrypt");
        byte[] raw = sKey.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv.getBytes()));
        byte[] encrypted1 = Base64.getDecoder().decode(sSrc);
        byte[] original = cipher.doFinal(encrypted1);
        return new String(original, StandardCharsets.UTF_8);
    }

    public static String to16ByteMD5(String sKey) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(sKey.getBytes());
        String hashedPwd = new BigInteger(1, md.digest()).toString(16);
        return hashedPwd.substring(8, 24);
    }
}
