package com.videocomm;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.tencent.bugly.crashreport.CrashReport;
import com.videocomm.utils.AppUtil;

public class MyApplication extends Application {

    private RefWatcher mRefWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化App工具类
        AppUtil.init(this);
        //逐木鸟
        CrashReport.initCrashReport(getApplicationContext(), "812f6917b7", true);
        //安装LeakCanary
        mRefWatcher = setupLeakCanary();
    }

    /**
     * 安装LeakCanary
     *
     * @return
     */
    private RefWatcher setupLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return RefWatcher.DISABLED;
        }
        return LeakCanary.install(this);
    }

    public static RefWatcher getRefWatcher(Context context) {
        MyApplication leakApplication = (MyApplication) context.getApplicationContext();
        return leakApplication.mRefWatcher;
    }
}
