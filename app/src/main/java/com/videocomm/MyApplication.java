package com.videocomm;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.crashreport.CrashReport;
import com.videocomm.utils.AppUtil;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化App工具类
        AppUtil.init(this);
        LeakCanary.install(this);
        CrashReport.initCrashReport(getApplicationContext(), "812f6917b7", true);
    }

}
