package com.videocomm.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author[wengCJ]
 * @version[创建日期，2019/12/16 0016]
 * @function[功能简介 SP存储类 要存储直接在下面添加set get 方法（一张表里不要存放太多信息）]
 **/

public class SpUtil {
    private static final String SPFILENAME = "_app";//sp文件名

    private static final String USERNAME = "username";//用户名
    private static final String ROOM = "room";//房间名
    private static final String APPID = "appid";//APP ID
    private static final String SERVERADDR = "server_addr";//服务器地址
    private static final String SERVERPORT = "server_port";//服务器端口

    private static final String HDSETTINGITEM = "hd_setting_item";//画质设置
    private static final String HDCUSTOMRESOLUTION = "hd_custom_resolution";
    private static final String HDCUSTOMFRAME = "hd_custom_frame";
    private static final String HDCUSTOMRATE = "hd_custom_rate";

    private static SharedPreferences.Editor mEdit;
    private static SharedPreferences sp;

    static {
        sp = AppUtil.getApp().getSharedPreferences(SPFILENAME, Context.MODE_PRIVATE);
        mEdit = sp.edit();
    }

    //保存
    public static void saveUserName(String userName) {
        mEdit.putString(USERNAME, userName);
        mEdit.apply();
    }

    public static void saveRoom(String room) {
        mEdit.putString(ROOM, room);
        mEdit.apply();
    }

    public static void saveAppId(String appId) {
        mEdit.putString(APPID, appId);
        mEdit.apply();
    }

    public static void saveServerAddr(String serverAddr) {
        mEdit.putString(SERVERADDR, serverAddr);
        mEdit.apply();
    }

    public static void saveServerPort(String serverPort) {
        mEdit.putString(SERVERPORT, serverPort);
        mEdit.apply();
    }

    public static void saveHdSettingItem(int num) {
        mEdit.putInt(HDSETTINGITEM, num);
        mEdit.apply();
    }

    public static void saveHdCustomResolution(int resolution) {
        mEdit.putInt(HDCUSTOMRESOLUTION, resolution);
        mEdit.apply();
    }

    public static void saveHdCustomFrame(int frame) {
        mEdit.putInt(HDCUSTOMFRAME, frame);
        mEdit.apply();
    }

    public static void saveHdCustomRate(int rate) {
        mEdit.putInt(HDCUSTOMRATE, rate);
        mEdit.apply();
    }

    //获取
    public static String getUsername() {
        return sp.getString(USERNAME, "");
    }

    public static String getRoom() {
        return sp.getString(ROOM, "");
    }

    public static String getAppid() {
        return sp.getString(APPID, "");
    }

    public static String getServerAddr() {
        return sp.getString(SERVERADDR, "");
    }

    public static String getServerPort() {
        return sp.getString(SERVERPORT, "");
    }

    public static int getHdSettingItem() {
        return sp.getInt(HDSETTINGITEM, 0);
    }

    public static int getHdCustomResolution() {
        return sp.getInt(HDCUSTOMRESOLUTION, 0);
    }

    public static int getHdCustomFrame() {
        return sp.getInt(HDCUSTOMFRAME, 0);
    }

    public static int getHdCustomRate() {
        return sp.getInt(HDCUSTOMRATE, 0);
    }

}
