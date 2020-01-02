package com.videocomm.activity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bairuitech.anychat.AnyChatBaseEvent;
import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.AnyChatDefine;
import com.videocomm.utils.ToastUtil;

/**
 *
 **/

public class BaseActivity extends AppCompatActivity implements AnyChatBaseEvent {

    protected AnyChatCoreSDK mAnyChatSDK;

    /**
     * 自己的用户ID
     */
    protected int mUserSelfId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatus();
        InitSDK();
        ToastUtil.show("BaseActivity onCreate");

    }

    /**
     * 初始化SDK
     */
    private void InitSDK() {
        if (mAnyChatSDK == null) {
            mAnyChatSDK = AnyChatCoreSDK.getInstance(this);
        }
        mAnyChatSDK.SetBaseEvent(this);
    }

    /**
     * 初始化状态栏(不包括全面屏)
     */
    private void initStatus() {
        //隐藏标题栏
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //隐藏状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    public AnyChatCoreSDK getAnyChatSDK() {
        return mAnyChatSDK;
    }

    /////////////////////////////////////////下面是AnyChatBaseEvent的接口，由子类自己根据需要去实现/////////////////////////////////////

    /**
     * 连接服务器消息, bSuccess表示是否连接成功
     *
     * @param bSuccess
     */
    @Override
    public void OnAnyChatConnectMessage(boolean bSuccess) {
        if (!bSuccess) {
            Log.i(this.getClass().getSimpleName(), "连接服务器失败，自动重连，请稍后...");
        }
    }

    /**
     * 用户进入房间消息，dwRoomId表示所进入房间的ID号，dwErrorCode表示是否进入房间：0成功进入，否则为出错代码
     *
     * @param dwRoomId
     * @param dwErrorCode
     */
    @Override
    public void OnAnyChatEnterRoomMessage(int dwRoomId, int dwErrorCode) {
        Log.i(this.getClass().getSimpleName(), "OnAnyChatEnterRoomMessage" + dwRoomId + "err:"
                + dwErrorCode);
    }


    /**
     * 用户登录消息，dwUserId表示自己的用户ID号，dwErrorCode表示登录结果：0 成功，否则为出错代码
     *
     * @param dwUserId
     * @param dwErrorCode
     */
    @Override
    public void OnAnyChatLoginMessage(int dwUserId, int dwErrorCode) {
        //保存自己的用户ID
        mUserSelfId = dwUserId;
    }

    /**
     * 房间在线用户消息，进入房间后触发一次，dwUserNum表示在线用户数（包含自己），dwRoomId表示房间ID
     *
     * @param dwUserNum
     * @param dwRoomId
     */
    @Override
    public void OnAnyChatOnlineUserMessage(int dwUserNum, int dwRoomId) {

    }

    /**
     * 用户进入/退出房间消息，dwUserId表示用户ID号，bEnter表示该用户是进入（TRUE）或离开（FALSE）房间
     *
     * @param dwUserId
     * @param bEnter
     */
    @Override
    public void OnAnyChatUserAtRoomMessage(int dwUserId, boolean bEnter) {

    }

    /**
     * 网络断开消息，该消息只有在客户端连接服务器成功之后，网络异常中断之时触发，dwErrorCode表示连接断开的原因
     *
     * @param dwErrorCode
     */
    @Override
    public void OnAnyChatLinkCloseMessage(int dwErrorCode) {
        Log.i(this.getClass().getSimpleName(), "OnAnyChatLinkCloseMessage" + dwErrorCode + "err:"
                + dwErrorCode);
    }
}