package com.videocomm.utils;

import android.widget.Toast;

/**
 * @author[wengCJ]
 * @version[创建日期，2019/12/16 0016]
 * @function[功能简介 Toast的工具类]
 **/

public class ToastUtil {

    public static void show(CharSequence text) {
        if (text == null) {
            return;
        }
        Toast.makeText(AppUtil.getApp(), text, Toast.LENGTH_SHORT).show();
    }
}
