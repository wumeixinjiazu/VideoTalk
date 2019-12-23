package com.videocomm.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author[wengCJ]
 * @version[创建日期，2019/12/19 0019]
 * @function[功能简介]
 **/
public class JsonUtil {

    /**
     * @param src 源数据（只接收这个格式的数据解析） {"version":"V2.0.51", "build":"Build Time:Dec 19 2019 09:56:57"}
     * @param parseStr 需要解析的json字段
     * @return
     */
    public static String jsonToStr(String src, String parseStr) {
        try {
            JSONObject jsonObject = new JSONObject(src);
            return jsonObject.getString(parseStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }
}

