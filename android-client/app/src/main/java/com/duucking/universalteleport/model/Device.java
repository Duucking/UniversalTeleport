package com.duucking.universalteleport.model;

public class Device {
    String IP;
    String deviceName;
    String Key;
    String deviceType;

    public Device(String IP, String deviceName, String Key, String deviceType) {
        this.IP = IP;
        this.deviceName = deviceName;
        this.Key = Key;
        this.deviceType = deviceType;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getKey() {
        return Key;
    }

    public void setKey(String key) {
        Key = key;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
