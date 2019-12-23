package com.videocomm;

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

    private String mUserName;
    private String mRoom;
    private String tag = this.getClass().getSimpleName();
    private LinearLayout llVideoControl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        mUserName = getIntent().getStringExtra("user_name");
        mRoom = getIntent().getStringExtra("room");

        initView();

        initSdk();

        initLogin();

        initAudioVideo();


    }

    /**
     * 音视频交互
     */
    private void initAudioVideo() {
        //5.2  设置必要的参数
        //设置本地视频采集随着设备而旋转而处理
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_AUTOROTATION, 1);

        //5.3  摄像头硬件初始化
        // 启动 AnyChat 传感器监听
        anyChatSDK.mSensorHelper.InitSensor(this);
        // 初始化 Camera 上下文句柄
        AnyChatCoreSDK.mCameraHelper.SetContext(this);

        //设置 SURFACE_TYPE_PUSH_BUFFERS 模式
        mSurfaceLocal.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 打开本地视频预览，开始采集本地视频数据
        mSurfaceLocal.getHolder().addCallback(AnyChatCoreSDK.mCameraHelper);


        // 如果是采用Java视频采集，则需要设置Surface的CallBack
        if (AnyChatCoreSDK
                .GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER) == AnyChatDefine.VIDEOCAP_DRIVER_JAVA) {
            mSurfaceLocal.getHolder().addCallback(AnyChatCoreSDK.mCameraHelper);
        }

        mSurfaceLocal.setZOrderOnTop(true);
        mSurfaceLocal.setZOrderMediaOverlay(true);


    }

    private void initLogin() {
        AnyChatCoreSDK.SetSDKOptionString(AnyChatDefine.BRAC_SO_CLOUD_APPGUID, "fbe957d1-c25a-4992-9e75-d993294a5d56");
        anyChatSDK.Connect("cloud.anychat.cn", 8906);
        anyChatSDK.Login(mUserName, "");
    }

    private void initSdk() {
        if (anyChatSDK == null) {
            anyChatSDK = AnyChatCoreSDK.getInstance(this);
            anyChatSDK.SetBaseEvent(this);
            anyChatSDK.InitSDK(android.os.Build.VERSION.SDK_INT, 0);
            AnyChatCoreSDK.SetSDKOptionInt(
                    AnyChatDefine.BRAC_SO_LOCALVIDEO_AUTOROTATION,
                    LOCALVIDEOAUTOROTATION);
        }
    }

    /**
     * 初始化布局
     */
    private void initView() {
        mSurfaceLocal = findViewById(R.id.surface_local);
        mSurfaceRemote = findViewById(R.id.surface_remote);
        ImageButton ibCameraReset = findViewById(R.id.ib_camera_reset);
        ImageButton ibHangUp = findViewById(R.id.ib_hang_up);
        ImageButton ibHdSetting = findViewById(R.id.ib_hd_setting);
        llVideoControl = findViewById(R.id.ll_video_control);

        ibCameraReset.setOnClickListener(this);
        ibHangUp.setOnClickListener(this);
        ibHdSetting.setOnClickListener(this);

    }

    /**
     * @param dwUserId
     * @param numView  需要创建多少个view
     */
    private void createFlexView(int dwUserId, int numView) {
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
                anyChatSDK.mVideoHelper.SetVideoUser(index, dwUserId);
            }

            // mRemoteUserid 为通话目标对象的 userId;
            int index = anyChatSDK.mVideoHelper.bindVideo(mSurfaceRemote.getHolder());
            anyChatSDK.mVideoHelper.SetVideoUser(index, dwUserId);
            anyChatSDK.UserCameraControl(dwUserId, 1);
            anyChatSDK.UserSpeakControl(dwUserId, 1);
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
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        anyChatSDK.LeaveRoom(-1);
        anyChatSDK.Logout();
        anyChatSDK.removeEvent(this);
        anyChatSDK.Release();
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
            int sHourseID = Integer.valueOf(mRoom);
            anyChatSDK.EnterRoom(sHourseID, "");

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
        //打开本地视频, 第一个参数用-1 表示本地，也可以用本地的真实 userid
        anyChatSDK.UserCameraControl(-1, 1);
//打开本地音频
        anyChatSDK.UserSpeakControl(-1, 1);
    }

    /**
     * 用户进入/退出房间消息，dwUserId表示用户ID号，bEnter表示该用户是进入（TRUE）或离开（FALSE）房间
     *
     * @param dwUserId
     * @param bEnter
     */
    int defaultNumber = 0;

    @Override
    public void OnAnyChatUserAtRoomMessage(int dwUserId, boolean bEnter) {
        Log.i(tag, "用户进入/退出房间消息，dwUserId表示用户ID号，bEnter表示该用户是进入（TRUE）或离开（FALSE）房间:" + dwUserId + "-bEnter:" + bEnter);
        if (bEnter) {
//            createFlexView(dwUserId,1);
            // mRemoteUserid 为通话目标对象的 userId;

            // 如果是采用Java视频显示，则需要设置Surface的CallBack
            if (AnyChatCoreSDK
                    .GetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_DRIVERCTRL) == AnyChatDefine.VIDEOSHOW_DRIVER_JAVA) {
                int index = anyChatSDK.mVideoHelper.bindVideo(mSurfaceRemote
                        .getHolder());
                anyChatSDK.mVideoHelper.SetVideoUser(index, dwUserId);
            }

            int index = anyChatSDK.mVideoHelper.bindVideo(mSurfaceRemote.getHolder());
            anyChatSDK.mVideoHelper.SetVideoUser(index, dwUserId);
            anyChatSDK.UserCameraControl(dwUserId, 1);
            anyChatSDK.UserSpeakControl(dwUserId, 1);

//            createFlexView(dwUserId, 1);


        } else {
            llVideoControl.removeAllViews();
        }
    }

    @Override
    public void OnAnyChatLinkCloseMessage(int dwErrorCode) {

    }
}

