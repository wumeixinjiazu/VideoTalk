package com.videocomm;

import android.app.Application;

import com.videocomm.utils.AppUtil;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化App工具类
        AppUtil.init(this);
    }

}
