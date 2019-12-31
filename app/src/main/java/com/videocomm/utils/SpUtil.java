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
    private static final String HDCUSTOMRESOLUTION = "hd_custom_resolution";//自定义分辨率设置
    private static final String HDCUSTOMFRAME = "hd_custom_frame";//自定义帧率设置
    private static final String HDCUSTOMRATE = "hd_custom_rate";//自定义码率设置

    private static final String RECORD_WAY = "record_way";//录制方式
    private static final String RECORD_TYPE = "record_type";//录制类型
    private static final String RECORD_OBJECT = "record_object";//录制对象
    private static final String RECORD_MODE = "record_mode";//录制模式

    private static SharedPreferences.Editor mEdit;
    private static SharedPreferences sp;

    private static SpUtil mSpUtil;

    private SpUtil() {
        //不允许创建实例
    }

    public static SpUtil getInstance() {
        if (mSpUtil == null || sp == null || mEdit == null) {
            mSpUtil = new SpUtil();

            sp = AppUtil.getApp().getSharedPreferences(SPFILENAME, Context.MODE_PRIVATE);
            mEdit = sp.edit();
            return mSpUtil;
        }
        return mSpUtil;
    }

    //保存
    public void saveUserName(String userName) {
        mEdit.putString(USERNAME, userName);
        mEdit.apply();
    }

    public void saveRoom(String room) {
        mEdit.putString(ROOM, room);
        mEdit.apply();
    }

    public void saveAppId(String appId) {
        mEdit.putString(APPID, appId);
        mEdit.apply();
    }

    public void saveServerAddr(String serverAddr) {
        mEdit.putString(SERVERADDR, serverAddr);
        mEdit.apply();
    }

    public void saveServerPort(String serverPort) {
        mEdit.putString(SERVERPORT, serverPort);
        mEdit.apply();
    }

    public void saveHdSettingItem(int num) {
        mEdit.putInt(HDSETTINGITEM, num);
        mEdit.apply();
    }

    public void saveHdCustomResolution(int resolution) {
        mEdit.putInt(HDCUSTOMRESOLUTION, resolution);
        mEdit.apply();
    }

    public void saveHdCustomFrame(int frame) {
        mEdit.putInt(HDCUSTOMFRAME, frame);
        mEdit.apply();
    }

    public void saveHdCustomRate(int rate) {
        mEdit.putInt(HDCUSTOMRATE, rate);
        mEdit.apply();
    }

    //获取
    public String getUsername() {
        return sp.getString(USERNAME, "");
    }

    public String getRoom() {
        return sp.getString(ROOM, "");
    }

    public String getAppid() {
        return sp.getString(APPID, "");
    }

    public String getServerAddr() {
        return sp.getString(SERVERADDR, "");
    }

    public String getServerPort() {
        return sp.getString(SERVERPORT, "");
    }

    public int getHdSettingItem() {
        return sp.getInt(HDSETTINGITEM, 0);
    }

    public int getHdCustomResolution() {
        return sp.getInt(HDCUSTOMRESOLUTION, 0);
    }

    public int getHdCustomFrame() {
        return sp.getInt(HDCUSTOMFRAME, 0);
    }

    public int getHdCustomRate() {
        return sp.getInt(HDCUSTOMRATE, 0);
    }

}
