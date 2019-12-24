package com.videocomm;

import android.app.ProgressDialog;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bairuitech.anychat.AnyChatBaseEvent;
import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.AnyChatDefine;
import com.google.android.flexbox.FlexboxLayout;
import com.videocomm.dlgFragment.HdSettingFragment;
import com.videocomm.utils.DisplayUtil;
import com.videocomm.utils.ToastUtil;

import java.util.ArrayList;

/**
 * @author[Wengcj]
 * @version[创建日期，2019/12/16 0016]
 * @function[功能简介 视频通讯的Acitivity]
 **/

public class VideoActivity extends AbsActivity implements View.OnClickListener, AnyChatBaseEvent {

    private Camera mCamera;
    private SurfaceView mSurfaceLocal;
    private SurfaceView mSurfaceRemote;

    private static final String TAGSDK = "VCommSdk";
    private static final String TAGSDKEVENT = "VCommSdkEvent";


    public AnyChatCoreSDK anyChatSDK;

    private final int SHOWLOGINSTATEFLAG = 1; // 显示的按钮是登陆状态的标识
    private final int SHOWWAITINGSTATEFLAG = 2; // 显示的按钮是等待状态的标识
    private final int SHOWLOGOUTSTATEFLAG = 3; // 显示的按钮是登出状态的标识
    private final int LOCALVIDEOAUTOROTATION = 1; // 本地视频自动旋转控制

    private String tag = this.getClass().getSimpleName();

    private LinearLayout llVideoControl;

    private int[] mOnlineUser;

    private boolean bSelfVideoOpened = false; // 本地视频是否已打开

    private ProgressDialog dialog;

    private int curCallView = 0; //记录当前通话视频的view个数


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        Log.i(tag, "oncreate");
        initSdk();
        initView();
        initAudioVideo();
        initCallView();

    }

    private void initCallView() {
        //其他用户的userid
        mOnlineUser = anyChatSDK.GetOnlineUser();
        if (mOnlineUser.length > 0) {
            for (int i = 0; i < mOnlineUser.length; i++) {
                Log.i(tag, "接收LoginActivity传过来的Userid" + mOnlineUser[i]);
            }
            curCallView = mOnlineUser.length;
            createFlexView(mOnlineUser.length);
        } else {
            dialog = new ProgressDialog(this);
            dialog.setMessage("等待其他用户加入");
            dialog.show();
        }
    }

    /**
     * 音视频交互
     */
    private void initAudioVideo() {
        //设置 SURFACE_TYPE_PUSH_BUFFERS 模式
        mSurfaceLocal.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 打开本地视频预览，开始采集本地视频数据
        mSurfaceLocal.getHolder().addCallback(AnyChatCoreSDK.mCameraHelper);


        // 如果是采用Java视频采集，则需要设置Surface的CallBack
        if (AnyChatCoreSDK
                .GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER) == AnyChatDefine.VIDEOCAP_DRIVER_JAVA) {
            mSurfaceLocal.getHolder().addCallback(AnyChatCoreSDK.mCameraHelper);
        }


        anyChatSDK.UserCameraControl(-1, 1);// -1表示对本地视频进行控制，打开本地视频
        anyChatSDK.UserSpeakControl(-1, 1);// -1表示对本地音频进行控制，打开本地音频

        // 判断是否显示本地摄像头切换图标
        if (AnyChatCoreSDK
                .GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER) == AnyChatDefine.VIDEOCAP_DRIVER_JAVA) {
            if (AnyChatCoreSDK.mCameraHelper.GetCameraNumber() > 1) {
                // 默认打开前置摄像头
                AnyChatCoreSDK.mCameraHelper
                        .SelectVideoCapture(AnyChatCoreSDK.mCameraHelper.CAMERA_FACING_FRONT);
            }
        } else {
            String[] strVideoCaptures = anyChatSDK.EnumVideoCapture();
            if (strVideoCaptures != null && strVideoCaptures.length > 1) {
                // 默认打开前置摄像头
                for (int i = 0; i < strVideoCaptures.length; i++) {
                    String strDevices = strVideoCaptures[i];
                    if (strDevices.indexOf("Front") >= 0) {
                        anyChatSDK.SelectVideoCapture(strDevices);
                        break;
                    }
                }
            }
        }
    }

    private void initSdk() {
        anyChatSDK = AnyChatCoreSDK.getInstance(VideoActivity.this);
        anyChatSDK.SetBaseEvent(this);
        anyChatSDK.mSensorHelper.InitSensor(getApplicationContext());
        AnyChatCoreSDK.mCameraHelper.SetContext(getApplicationContext());
    }

    /**
     * 初始化布局
     */
    private void initView() {
        mSurfaceLocal = findViewById(R.id.surface_local);
//        mSurfaceRemote = findViewById(R.id.surface_remote);
        ImageButton ibCameraReset = findViewById(R.id.ib_camera_reset);
        ImageButton ibHangUp = findViewById(R.id.ib_hang_up);
        ImageButton ibHdSetting = findViewById(R.id.ib_hd_setting);
        llVideoControl = findViewById(R.id.ll_video_control);

        ibCameraReset.setOnClickListener(this);
        ibHangUp.setOnClickListener(this);
        ibHdSetting.setOnClickListener(this);

    }

    /**
     * @param numView 需要创建多少个view
     */
    private void createFlexView(int numView) {
        int width = DisplayUtil.getScreenWidth() / 4;
        int heigth = DisplayUtil.getScreenHeight() / 6;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, heigth);
        lp.rightMargin = DisplayUtil.dp2px(10);
        for (int i = 0; i < numView; i++) {
            SurfaceView mSurfaceRemote = new SurfaceView(VideoActivity.this);

            // 如果是采用Java视频显示，则需要设置Surface的CallBack
            if (AnyChatCoreSDK
                    .GetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_DRIVERCTRL) == AnyChatDefine.VIDEOSHOW_DRIVER_JAVA) {
                int index = anyChatSDK.mVideoHelper.bindVideo(mSurfaceRemote
                        .getHolder());
                anyChatSDK.mVideoHelper.SetVideoUser(index, mOnlineUser[i]);
            }

            int index = anyChatSDK.mVideoHelper.bindVideo(mSurfaceRemote.getHolder());
            anyChatSDK.mVideoHelper.SetVideoUser(index, mOnlineUser[i]);

            mSurfaceRemote.setZOrderOnTop(true);

            anyChatSDK.UserCameraControl(mOnlineUser[0], 1);
            anyChatSDK.UserSpeakControl(mOnlineUser[0], 1);

            llVideoControl.addView(mSurfaceRemote, lp);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_hd_setting://分辨率设置
                new HdSettingFragment().show(getSupportFragmentManager(), "HdSettingDialog");
                break;
            case R.id.ib_hang_up://挂断
                finish();
                break;
            case R.id.ib_camera_reset://相机翻转
                // 如果是采用Java视频采集，则在Java层进行摄像头切换
                if (AnyChatCoreSDK
                        .GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER) == AnyChatDefine.VIDEOCAP_DRIVER_JAVA) {
                    AnyChatCoreSDK.mCameraHelper.SwitchCamera();
                    return;
                }

                String strVideoCaptures[] = anyChatSDK.EnumVideoCapture();
                String temp = anyChatSDK.GetCurVideoCapture();
                for (int i = 0; i < strVideoCaptures.length; i++) {
                    if (!temp.equals(strVideoCaptures[i])) {
                        anyChatSDK.UserCameraControl(-1, 0);
                        bSelfVideoOpened = false;
                        anyChatSDK.SelectVideoCapture(strVideoCaptures[i]);
                        anyChatSDK.UserCameraControl(-1, 1);
                        break;
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(tag, "onStart");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(tag, "onResume");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(tag, "onPause");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(tag, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(tag, "onDestroy");
        anyChatSDK.LeaveRoom(-1);
        anyChatSDK.Logout();
        anyChatSDK.removeEvent(this);
        anyChatSDK.UserCameraControl(-1, 0);// -1表示对本地视频进行控制，打开本地视频
        anyChatSDK.UserSpeakControl(-1, 0);// -1表示对本地音频进行控制，打开本地音频
        anyChatSDK.mSensorHelper.DestroySensor();
    }

    /**
     * // 连接服务器消息, bSuccess表示是否连接成功
     *
     * @param bSuccess
     */
    @Override
    public void OnAnyChatConnectMessage(boolean bSuccess) {

        Log.i(tag, "连接服务器消息" + bSuccess);
        if (!bSuccess) {
            ToastUtil.show("连接服务器失败，自动重连，请稍后...");
            System.out.println("connect failed");
        }
        ToastUtil.show("连接服务器成功");
    }

    /**
     * // 用户登录消息，dwUserId表示自己的用户ID号，dwErrorCode表示登录结果：0 成功，否则为出错代码
     *
     * @param dwUserId
     * @param dwErrorCode
     */
    @Override
    public void OnAnyChatLoginMessage(int dwUserId, int dwErrorCode) {
        Log.i(tag, "用户登录消息 dwUserId表示自己的用户ID号" + dwUserId + "dwErrorCode:" + dwErrorCode);
        if (dwErrorCode == 0) {
            Log.i(tag, "登陆成功");
        } else {
            ToastUtil.show("登录失败，errorCode：" + dwErrorCode);
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
        Log.i(tag, "用户进入房间消息 dwRoomId表示所进入房间的ID号:" + dwRoomId + "-dwErrorCode:" + dwErrorCode);

    }

    /**
     * 房间在线用户消息，进入房间后触发一次，dwUserNum表示在线用户数（包含自己），dwRoomId表示房间ID
     *
     * @param dwUserNum
     * @param dwRoomId
     */
    @Override
    public void OnAnyChatOnlineUserMessage(int dwUserNum, int dwRoomId) {
        Log.i(tag, "房间在线用户消息 dwUserNum表示在线用户数（包含自己）:" + dwUserNum + "-dwRoomId表示房间ID:" + dwRoomId);

    }

    /**
     * 用户进入/退出房间消息，dwUserId表示用户ID号，bEnter表示该用户是进入（TRUE）或离开（FALSE）房间
     *
     * @param dwUserId
     * @param bEnter
     */
    @Override
    public void OnAnyChatUserAtRoomMessage(int dwUserId, boolean bEnter) {
        Log.i(tag, "用户进入/退出房间消息，dwUserId表示用户ID号，bEnter表示该用户是进入（TRUE）或离开（FALSE）房间:" + dwUserId + "-bEnter:" + bEnter);
        if (bEnter) {
            //进入房间
            mOnlineUser = anyChatSDK.GetOnlineUser();
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }

            Log.i(tag, "进来一个用户 ID为" + dwUserId);
            Log.i(tag, "进来一个用户 用户名为" + anyChatSDK.GetUserName(dwUserId));
            if (curCallView == 0) {
                curCallView = mOnlineUser.length;
                createFlexView(mOnlineUser.length);
            } else {
                createFlexView(mOnlineUser.length - curCallView);
            }
        } else {
            //退出房间
            Log.i(tag, "退出一个用户 ID为" + dwUserId);
            Log.i(tag, "退出一个用户 用户名为" + anyChatSDK.GetUserName(dwUserId));
            anyChatSDK.UserCameraControl(dwUserId, 0);
            anyChatSDK.UserSpeakControl(dwUserId, 0);
            curCallView --;//减少一个正在通话的View
            if (dwUserId != LoginActivity.mUserSelfId) { //第一次进来会走到这个方法，需要做个判断，防止空指针
                for (int i = 0; i < mOnlineUser.length; i++) {
                    if (dwUserId == mOnlineUser[i]) {
                        llVideoControl.removeViewAt(i);
                    }
                }
            }
            ToastUtil.show("用户" + dwUserId + "离开房间");
        }
    }

    /**
     * 网络断开消息，该消息只有在客户端连接服务器成功之后，网络异常中断之时触发，dwErrorCode表示连接断开的原因
     *
     * @param dwErrorCode
     */
    @Override
    public void OnAnyChatLinkCloseMessage(int dwErrorCode) {
        anyChatSDK.LeaveRoom(-1);
        anyChatSDK.Logout();
    }
}

