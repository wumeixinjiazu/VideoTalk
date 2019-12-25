package com.videocomm.bean;

/**
 * @author[wengCJ]
 * @version[创建日期，2019/12/24 0024]
 * @function[功能简介 存储用户的信息]
 **/
public class UserBean {
    private String userName;
    private int userId;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
