package com.videocomm.utils;

import android.app.Application;

/**
 * @author[wengCJ]
 * @version[创建日期，2019/12/16 0016]
 * @function[功能简介 存放一些主要共用的工具类]
 **/

public class AppUtil {
    private static Application app;

    public static Application getApp() {
        return app;
    }

    public static void init(Application app) {
        AppUtil.app = app;
    }


}
