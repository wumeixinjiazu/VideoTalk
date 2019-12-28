package com.videocomm;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bairuitech.anychat.AnyChatBaseEvent;
import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.AnyChatDefine;
import com.bairuitech.anychat.AnyChatRecordEvent;
import com.videocomm.dlgFragment.HdSettingFragment;
import com.videocomm.utils.DisplayUtil;
import com.videocomm.utils.ToastUtil;

import java.lang.ref.WeakReference;

/**
 * @author[Wengcj]
 * @version[创建日期，2019/12/16 0016]
 * @function[功能简介 视频通讯的Acitivity]
 **/

public class VideoActivity extends AbsActivity implements View.OnClickListener, AnyChatBaseEvent, AnyChatRecordEvent {

    private static final String tagSdk = "VCommSdk";

    private String tag = this.getClass().getSimpleName();

    private SurfaceView mSurfaceLocal;

    public AnyChatCoreSDK anyChatSDK;

    private LinearLayout llVideoControl;

    private ProgressDialog mDialog;

    private ImageButton ibRecord;

    private ImageView ivRecordState;

    /**
     * 存储用户的列表 用于用户退出时可以判断要移除的view
     */
    SparseIntArray userList = new SparseIntArray();

    /**
     * 在线用户id数组
     */
    private int[] mOnlineUser;

    /**
     * 计时器
     */
    private Chronometer chronometer;

    /**
     * 记录是否正在录制 默认关闭
     */
    public boolean isRecordingState = false;

    /**
     * 记录是否开启录制 默认关闭
     */
    private boolean isRecordState = false;

    private VideoHandler mHandler = new VideoHandler(this);
    private ConstraintLayout mVideoMainLayout;

    static class VideoHandler extends Handler {
        WeakReference<Activity> refActivity;

        public VideoHandler(Activity activity) {
            refActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (refActivity.get() != null) {
                VideoActivity activity = (VideoActivity) refActivity.get();
                if (activity.isRecordingState) {
                    activity.ivRecordState.setBackgroundResource(R.drawable.ic_recording_stop);//切换图片
                    activity.isRecordingState = false;
                } else {
                    activity.ivRecordState.setBackgroundResource(R.drawable.ic_recording_start);//切换图片
                    activity.isRecordingState = true;
                }

                activity.mHandler.sendEmptyMessageDelayed(0, 1000);//继续一秒发送一条空消息，实现切换
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        initSdk();
        initView();
        initAudioVideo();
        initCallView();


    }

    /**
     * 根据房间人数 初始化对应的视频
     */
    private void initCallView() {
        mOnlineUser = anyChatSDK.GetOnlineUser();//获取在线的用户的id 不包括自己
        userList.clear();

        if (mOnlineUser.length > 0) {
            for (int i = 0; i < mOnlineUser.length; i++) {
                Log.i(tag, "在线的用户的id" + mOnlineUser[i]);
                userList.put(Math.abs(mOnlineUser[i]), Math.abs(mOnlineUser[i]));//把用户的id存进数组(put进去的数值一定要正值 负值的话值都是往 0 索引添加)
                createOtherVideoView(mOnlineUser[i]);
            }
        } else {
            mDialog = new ProgressDialog(this);
            mDialog.setMessage("等待其他用户加入");
            mDialog.show();
        }
    }

    /**
     * 音视频交互
     */
    private void initAudioVideo() {

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

    /**
     * 初始化SDK
     */
    private void initSdk() {
        anyChatSDK = AnyChatCoreSDK.getInstance(VideoActivity.this);
        anyChatSDK.SetBaseEvent(this);
        anyChatSDK.mSensorHelper.InitSensor(getApplicationContext());
        anyChatSDK.CameraAutoFocus();//自动对焦
        AnyChatCoreSDK.mCameraHelper.SetContext(getApplicationContext());
        //设置录制拍照的回调
        anyChatSDK.SetRecordSnapShotEvent(this);

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
        ImageButton ibMore = findViewById(R.id.ib_more);
        ibRecord = findViewById(R.id.ib_record);
        llVideoControl = findViewById(R.id.ll_video_control);
        ivRecordState = findViewById(R.id.iv_record_state);
        chronometer = findViewById(R.id.chronometer);
        mVideoMainLayout = findViewById(R.id.video_main);


        ibCameraReset.setOnClickListener(this);
        ibHangUp.setOnClickListener(this);
        ibHdSetting.setOnClickListener(this);
        ibMore.setOnClickListener(this);
        ibRecord.setOnClickListener(this);


    }

    /**
     * 创建其他用户的视频界面
     *
     * @param userId 其他用户的Id
     */
    private void createOtherVideoView(int userId) {
        int width = DisplayUtil.getScreenWidth() / 3;
        int heigth = DisplayUtil.getScreenHeight() / 4;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, heigth);
        lp.rightMargin = DisplayUtil.dp2px(10);
        SurfaceView mSurfaceRemote = new SurfaceView(VideoActivity.this);
        boolean isSmartVideo = true;//记录是否是小视频
        // 如果是采用Java视频显示，则需要设置Surface的CallBack
        if (AnyChatCoreSDK
                .GetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_DRIVERCTRL) == AnyChatDefine.VIDEOSHOW_DRIVER_JAVA) {
            int index = anyChatSDK.mVideoHelper.bindVideo(mSurfaceRemote
                    .getHolder());
            anyChatSDK.mVideoHelper.SetVideoUser(index, userId);
            mSurfaceRemote.setTag(isSmartVideo);
        }
        mSurfaceRemote.setOnClickListener(this);
        mSurfaceRemote.setZOrderOnTop(true);

        anyChatSDK.UserCameraControl(userId, 1);//打开对方视频进行控制，打开本地视频
        anyChatSDK.UserSpeakControl(userId, 1);//打开对方音频进行控制，打开本地视频

        llVideoControl.addView(mSurfaceRemote, lp);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_hd_setting://分辨率设置
                new HdSettingFragment().show(getSupportFragmentManager(), "HdSettingDialog");
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
                        anyChatSDK.SelectVideoCapture(strVideoCaptures[i]);
                        anyChatSDK.UserCameraControl(-1, 1);
                        break;
                    }
                }
                break;
            case R.id.ib_hang_up://挂断
                finish();
                break;
            case R.id.ib_record:
                if (!isRecordState) {
                    startRecord();//开始录像
                } else {
                    stopRecord();//停止录像
                }

                break;
            case R.id.ib_more://更多功能

                break;
            default:
                for (int i = 0; i < llVideoControl.getChildCount(); i++) {
                    SurfaceView curClickView = (SurfaceView) llVideoControl.getChildAt(i);
                    if (v == curClickView) {
                        switchVideo(curClickView, i);
                    }else {
                        switchVideo(curClickView, i);
                    }
                }
                break;
        }
    }

    /**
     * 切换视频
     *
     * @param curClickView 当前点击切换的视频
     */
    private void switchVideo(SurfaceView curClickView, int position) {
        ToastUtil.show("点中了第" + position + "个"+curClickView.getTag());
        mOnlineUser = anyChatSDK.GetOnlineUser();
        if ((boolean) curClickView.getTag()) {
            //其他用户视频切换为大视频
            //停止双方音视频的传输
            anyChatSDK.UserCameraControl(-1, 0);
            anyChatSDK.UserSpeakControl(-1, 0);
//
            anyChatSDK.UserCameraControl(mOnlineUser[position], 0);
            anyChatSDK.UserSpeakControl(mOnlineUser[position], 0);

            llVideoControl.removeView(curClickView);
            mVideoMainLayout.removeView(mSurfaceLocal);

            mSurfaceLocal = new SurfaceView(VideoActivity.this);
            curClickView = new SurfaceView(VideoActivity.this);

            curClickView.setOnClickListener(this);
            curClickView.setTag(false);

            // 如果是采用Java视频采集，则需要设置Surface的CallBack
            if (AnyChatCoreSDK
                    .GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER) == AnyChatDefine.VIDEOCAP_DRIVER_JAVA) {
                curClickView.getHolder().addCallback(AnyChatCoreSDK.mCameraHelper);
            }

            // 如果是采用Java视频显示，则需要设置Surface的CallBack
            if (AnyChatCoreSDK
                    .GetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_DRIVERCTRL) == AnyChatDefine.VIDEOSHOW_DRIVER_JAVA) {
                int index = anyChatSDK.mVideoHelper.bindVideo(mSurfaceLocal
                        .getHolder());
                anyChatSDK.mVideoHelper.SetVideoUser(index, mOnlineUser[position]);
            }


            //开启双方音视频的传输
            anyChatSDK.UserCameraControl(-1, 1);
            anyChatSDK.UserSpeakControl(-1, 1);

            anyChatSDK.UserCameraControl(mOnlineUser[position], 1);
            anyChatSDK.UserSpeakControl(mOnlineUser[position], 1);

            int width = DisplayUtil.getScreenWidth() / 3;
            int heigth = DisplayUtil.getScreenHeight() / 4;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, heigth);
            lp.rightMargin = DisplayUtil.dp2px(10);

            llVideoControl.addView(curClickView, position, lp);
            mVideoMainLayout.addView(mSurfaceLocal, new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,ConstraintLayout.LayoutParams.MATCH_PARENT));
        } else {
            //自己切换为小视频
            //停止双方音视频的传输
            anyChatSDK.UserCameraControl(-1, 0);
            anyChatSDK.UserSpeakControl(-1, 0);
//
            anyChatSDK.UserCameraControl(mOnlineUser[position], 0);
            anyChatSDK.UserSpeakControl(mOnlineUser[position], 0);

            llVideoControl.removeView(curClickView);
            mVideoMainLayout.removeView(mSurfaceLocal);

            mSurfaceLocal = new SurfaceView(VideoActivity.this);
            curClickView = new SurfaceView(VideoActivity.this);

            curClickView.setOnClickListener(this);
            curClickView.setTag(true);

            // 如果是采用Java视频采集，则需要设置Surface的CallBack
            if (AnyChatCoreSDK
                    .GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER) == AnyChatDefine.VIDEOCAP_DRIVER_JAVA) {
                mSurfaceLocal.getHolder().addCallback(AnyChatCoreSDK.mCameraHelper);
            }

            // 如果是采用Java视频显示，则需要设置Surface的CallBack
            if (AnyChatCoreSDK
                    .GetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_DRIVERCTRL) == AnyChatDefine.VIDEOSHOW_DRIVER_JAVA) {
                int index = anyChatSDK.mVideoHelper.bindVideo(curClickView
                        .getHolder());
                anyChatSDK.mVideoHelper.SetVideoUser(index, mOnlineUser[position]);
            }


            //开启双方音视频的传输
            anyChatSDK.UserCameraControl(-1, 1);
            anyChatSDK.UserSpeakControl(-1, 1);

            anyChatSDK.UserCameraControl(mOnlineUser[position], 1);
            anyChatSDK.UserSpeakControl(mOnlineUser[position], 1);
            int width = DisplayUtil.getScreenWidth() / 3;
            int heigth = DisplayUtil.getScreenHeight() / 4;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, heigth);
            lp.rightMargin = DisplayUtil.dp2px(10);

            llVideoControl.addView(curClickView, position, lp);
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
            mVideoMainLayout.addView(mSurfaceLocal,layoutParams);

        }


    }

    private void stopRecord() {
        int flag = AnyChatDefine.ANYCHAT_RECORD_FLAGS_VIDEO + AnyChatDefine.ANYCHAT_RECORD_FLAGS_AUDIO;
        int state = anyChatSDK.StreamRecordCtrlEx(-1, 0, flag, 12306, "");

        if (isRecordState) {
            ibRecord.setBackgroundResource(R.drawable.ic_record_default);//切换成暂停录制
            isRecordState = false;//关闭录制
            isRecordingState = false;//关闭正在录制
        }
        chronometer.stop();//计时器停止
        chronometer.setVisibility(View.GONE);//录制计时器隐藏
        ivRecordState.setVisibility(View.GONE);//正在录制图标隐藏
        mHandler.removeCallbacksAndMessages(null);
    }

    private void startRecord() {

        int flag = AnyChatDefine.ANYCHAT_RECORD_FLAGS_VIDEO + AnyChatDefine.ANYCHAT_RECORD_FLAGS_AUDIO + AnyChatDefine.ANYCHAT_RECORD_FLAGS_ABREAST + AnyChatDefine.ANYCHAT_RECORD_FLAGS_STEREO;
        int state = anyChatSDK.StreamRecordCtrlEx(-1, 1, flag, 12306, "");
        if (state != 0) {
            ToastUtil.show("录制失败" + state);
            return;
        }

        if (!isRecordState) {
            ibRecord.setBackgroundResource(R.drawable.ic_record_pressed);//切换成开始录制
            isRecordState = true;//开始录制
            isRecordingState = true;//开始正在录制
        }
        chronometer.setBase(SystemClock.elapsedRealtime());//计时器清零
        chronometer.start();//开始计时
        chronometer.setVisibility(View.VISIBLE);//录制计时器显示
        ivRecordState.setVisibility(View.VISIBLE);//正在录制图标显示

        mHandler.sendEmptyMessageDelayed(0, 1000);//一秒后发送一个空消息

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //用户切回来的时候走这个方法
        //这时候要对用户的音视频进行打开操作
        mOnlineUser = anyChatSDK.GetOnlineUser();//更新一下在线人数
        for (int i = 0; i < mOnlineUser.length; i++) {
            //打开其他用户的音视频
            anyChatSDK.UserCameraControl(mOnlineUser[i], 1);
            anyChatSDK.UserSpeakControl(mOnlineUser[i], 1);

            // 如果是采用Java视频显示，则需要设置Surface的CallBack
            SurfaceView surfaceViewRemote = (SurfaceView) llVideoControl.getChildAt(i);
            if (AnyChatCoreSDK
                    .GetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_DRIVERCTRL) == AnyChatDefine.VIDEOSHOW_DRIVER_JAVA) {
                int index = anyChatSDK.mVideoHelper.bindVideo(surfaceViewRemote
                        .getHolder());
                anyChatSDK.mVideoHelper.SetVideoUser(index, mOnlineUser[i]);
            }
        }

        anyChatSDK.UserCameraControl(-1, 1);//-1表示对本地视频进行控制，打开本地视频
        anyChatSDK.UserSpeakControl(-1, 1);//-1表示对本地音频进行控制，打开本地视频
    }

    @Override
    protected void onPause() {
        super.onPause();
        //用户切出去的时候走这个方法
        // 这时候要对用户的音视频进行关闭操作
        mOnlineUser = anyChatSDK.GetOnlineUser();//更新一下在线人数
        for (int i = 0; i < mOnlineUser.length; i++) {
            //关闭其他用户的音视频
            anyChatSDK.UserCameraControl(mOnlineUser[i], 0);
            anyChatSDK.UserSpeakControl(mOnlineUser[i], 0);
        }

        anyChatSDK.UserCameraControl(-1, 0);//-1表示对本地视频进行控制，关闭本地视频
        anyChatSDK.UserSpeakControl(-1, 0);//-1表示对本地音频进行控制，关闭本地视频

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(tag, "onDestroy");
        llVideoControl.removeAllViews();
        anyChatSDK.UserCameraControl(-1, 0);// -1表示对本地视频进行控制，关闭本地视频
        anyChatSDK.UserSpeakControl(-1, 0);// -1表示对本地音频进行控制，关闭本地音频
        anyChatSDK.removeEvent(this);
        anyChatSDK.LeaveRoom(-1);
        anyChatSDK.Logout();
        anyChatSDK.mSensorHelper.DestroySensor();
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;//防止内存泄露
    }

    /**
     * 连接服务器消息, bSuccess表示是否连接成功
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
     * 用户登录消息，dwUserId表示自己的用户ID号，dwErrorCode表示登录结果：0 成功，否则为出错代码
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
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }

            Log.i(tag, "进来一个用户 ID为" + dwUserId);
            Log.i(tag, "进来一个用户 用户名为" + anyChatSDK.GetUserName(dwUserId));
            userList.put(Math.abs(dwUserId), Math.abs(dwUserId));//把用户的id存进数组
            createOtherVideoView(dwUserId);//创建一个用户视图
        } else {
            //退出房间
            Log.i(tag, "退出一个用户 ID为" + dwUserId);
            Log.i(tag, "退出一个用户 用户名为" + anyChatSDK.GetUserName(dwUserId));
            anyChatSDK.UserCameraControl(dwUserId, 0);
            anyChatSDK.UserSpeakControl(dwUserId, 0);
            if (dwUserId != LoginActivity.mUserSelfId) { //第一次进来会走到这个方法，需要做个判断，防止空指针
                Log.i(tag, "退出一个用户 ID索引" + userList.indexOfValue(Math.abs(dwUserId)));
                Log.i(tag, "退出一个用户 用户名为" + anyChatSDK.GetUserName(dwUserId));

                llVideoControl.removeViewAt(userList.indexOfValue(Math.abs(dwUserId)));//根据退出的用户id查出索引移除view
                userList.removeAt(userList.indexOfValue(Math.abs(dwUserId)));//移除索引对应的数值

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

    }

    /**
     * 视频录制完成事件 StreamRecordCtrlEx 录像完成之后，将会触发该回调事件
     *
     * @param dwUserId    被录制用户 ID
     * @param dwErrorCode 错误代码
     * @param lpFileName  文件保存路径
     * @param dwElapse    录像时长，单位：秒
     * @param dwFlags     录像标志
     * @param dwParam     用户自定义参数，整型
     * @param lpUserStr   用户自定义参数，字符串类型
     */
    @Override
    public void OnAnyChatRecordEvent(int dwUserId, int dwErrorCode, String lpFileName, int dwElapse, int dwFlags, int dwParam, String lpUserStr) {
        Log.i(tag, "被录制用户 ID" + dwUserId);
        Log.i(tag, "文件保存路径" + lpFileName);
        Log.i(tag, "录像时长，单位：秒" + dwElapse);
        Log.i(tag, "录像标志" + dwFlags);
        Log.i(tag, "用户自定义参数，整型" + dwParam);
        ;
        Log.i(tag, "用户自定义参数，字符串类型" + lpUserStr);
        ;
        ToastUtil.show("录制结束，文件保存在" + lpFileName);
    }

    /**
     * 图像抓拍完成事件
     *
     * @param dwUserId
     * @param dwErrorCode
     * @param lpFileName
     * @param dwFlags
     * @param dwParam
     * @param lpUserStr
     */
    @Override
    public void OnAnyChatSnapShotEvent(int dwUserId, int dwErrorCode, String lpFileName, int dwFlags, int dwParam, String lpUserStr) {

    }

}

