package com.videocomm.activity;

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
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;

import com.bairuitech.anychat.AnyChatBaseEvent;
import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.AnyChatDefine;
import com.bairuitech.anychat.AnyChatRecordEvent;
import com.bairuitech.anychat.AnyChatTransDataEvent;
import com.videocomm.R;
import com.videocomm.dlgFragment.HdSettingFragment;
import com.videocomm.dlgFragment.MoreFeatureFragment;
import com.videocomm.utils.DisplayUtil;
import com.videocomm.utils.ToastUtil;

import java.lang.ref.WeakReference;

/**
 * @author[Wengcj]
 * @version[创建日期，2019/12/16 0016]
 * @function[功能简介 视频通讯的Acitivity]
 **/

public class VideoActivity extends BaseActivity implements View.OnClickListener, AnyChatBaseEvent, AnyChatRecordEvent, AnyChatTransDataEvent {

    private static final String tagSdk = "VCommSdk";

    private String tag = this.getClass().getSimpleName();

    private SurfaceView mSurfaceLocal;

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
    /**
     * 视频界面所有的功能按钮
     */
    private LinearLayout mLlControl;

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
        initSDK();
        initEvent();
        initView();
        initAudioVideo();
        initCallView();
    }

    /**
     * 初始化各种回调事件
     */
    private void initEvent() {
        //设置录制拍照的回调
        mAnyChatSDK.SetRecordSnapShotEvent(this);
        mAnyChatSDK.SetTransDataEvent(this);
    }

    /**
     * 根据房间人数 初始化对应的视频
     */
    private void initCallView() {
        mOnlineUser = mAnyChatSDK.GetOnlineUser();//获取在线的用户的id 不包括自己
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

        mAnyChatSDK.UserCameraControl(-1, 1);// -1表示对本地视频进行控制，打开本地视频
        mAnyChatSDK.UserSpeakControl(-1, 1);// -1表示对本地音频进行控制，打开本地音频

        // 判断是否显示本地摄像头切换图标
        if (AnyChatCoreSDK
                .GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER) == AnyChatDefine.VIDEOCAP_DRIVER_JAVA) {
            if (AnyChatCoreSDK.mCameraHelper.GetCameraNumber() > 1) {
                // 默认打开前置摄像头
                AnyChatCoreSDK.mCameraHelper
                        .SelectVideoCapture(AnyChatCoreSDK.mCameraHelper.CAMERA_FACING_FRONT);
            }
        } else {
            String[] strVideoCaptures = mAnyChatSDK.EnumVideoCapture();
            if (strVideoCaptures != null && strVideoCaptures.length > 1) {
                // 默认打开前置摄像头
                for (int i = 0; i < strVideoCaptures.length; i++) {
                    String strDevices = strVideoCaptures[i];
                    if (strDevices.indexOf("Front") >= 0) {
                        mAnyChatSDK.SelectVideoCapture(strDevices);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 初始化SDK
     */
    private void initSDK() {
        mAnyChatSDK = AnyChatCoreSDK.getInstance(VideoActivity.this);
        mAnyChatSDK.mSensorHelper.InitSensor(getApplicationContext());
        mAnyChatSDK.CameraAutoFocus();//自动对焦
        AnyChatCoreSDK.mCameraHelper.SetContext(getApplicationContext());
    }

    /**
     * 初始化布局
     */
    private void initView() {
        mSurfaceLocal = findViewById(R.id.surface_local);
        ImageButton ibCameraReset = findViewById(R.id.ib_camera_reset);
        ImageButton ibHangUp = findViewById(R.id.ib_hang_up);
        ImageButton ibHdSetting = findViewById(R.id.ib_hd_setting);
        ImageButton ibMore = findViewById(R.id.ib_more);
        ibRecord = findViewById(R.id.ib_record);
        llVideoControl = findViewById(R.id.ll_video_control);
        ivRecordState = findViewById(R.id.iv_record_state);
        chronometer = findViewById(R.id.chronometer);
        mVideoMainLayout = findViewById(R.id.video_main);
        mLlControl = findViewById(R.id.ll_control);

        mSurfaceLocal.setTag(mUserSelfId);

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
        int heigth = DisplayUtil.getScreenHeight() / 3;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, heigth);
        lp.rightMargin = DisplayUtil.dp2px(10);
        SurfaceView mSurfaceRemote = new SurfaceView(VideoActivity.this);
        // 如果是采用Java视频显示，则需要设置Surface的CallBack
        if (AnyChatCoreSDK
                .GetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_DRIVERCTRL) == AnyChatDefine.VIDEOSHOW_DRIVER_JAVA) {
            int index = mAnyChatSDK.mVideoHelper.bindVideo(mSurfaceRemote
                    .getHolder());
            mAnyChatSDK.mVideoHelper.SetVideoUser(index, userId);
        }

        mSurfaceRemote.setTag(userId);//记录视频的用户id

        mSurfaceRemote.setOnClickListener(this);
        mSurfaceRemote.setZOrderOnTop(true);

        mAnyChatSDK.UserCameraControl(userId, 1);//打开对方视频进行控制，打开本地视频
        mAnyChatSDK.UserSpeakControl(userId, 1);//打开对方音频进行控制，打开本地视频

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

                String[] strVideoCaptures = mAnyChatSDK.EnumVideoCapture();
                String temp = mAnyChatSDK.GetCurVideoCapture();
                for (int i = 0; i < strVideoCaptures.length; i++) {
                    if (!temp.equals(strVideoCaptures[i])) {
                        mAnyChatSDK.UserCameraControl(-1, 0);
                        mAnyChatSDK.SelectVideoCapture(strVideoCaptures[i]);
                        mAnyChatSDK.UserCameraControl(-1, 1);
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
                new MoreFeatureFragment(mAnyChatSDK,mUserSelfId).show(getSupportFragmentManager(), "MoreFeatureFragment");
                break;
            default:
                for (int i = 0; i < userList.size(); i++) {
                    SurfaceView clickSmartView = (SurfaceView) llVideoControl.getChildAt(i);
                    //获取被点击的视频判断是否与小视频控件一样
                    if (v == clickSmartView) {
                        switchVideo(clickSmartView, i);
                    }
                }
                break;
        }
    }

    /**
     * 大小视频切换 （主要是把之前绑定的控件SurfaceView移除掉，然后重新设置渲染）
     * <p>
     * 一开始大视频都是播放自己的画面 小视频都是其他用户的画面
     *
     * @param clickSmartView 当前点击切的视频
     * @param position       被点击的小视频的位置
     */
    private void switchVideo(SurfaceView clickSmartView, int position) {

        ToastUtil.show("点中了第" + position + "个");

        //获取保存在 clickSmartView 中记录的参数
        int curSmartUserId = (int) clickSmartView.getTag();

        //获取保存在 mSurfaceLocal 中记录的参数
        int curBigUserId = (int) mSurfaceLocal.getTag();

        //停止自己的音视频的传输
        mAnyChatSDK.UserCameraControl(-1, 0);
        mAnyChatSDK.UserSpeakControl(-1, 0);

        //停止对方的音视频的传输
        mAnyChatSDK.UserCameraControl(curSmartUserId, 0);
        mAnyChatSDK.UserSpeakControl(curSmartUserId, 0);

        //移除控件
        llVideoControl.removeView(clickSmartView);
        mVideoMainLayout.removeView(mSurfaceLocal);

        //创建控件
        mSurfaceLocal = new SurfaceView(VideoActivity.this);
        clickSmartView = new SurfaceView(VideoActivity.this);

        //设置监听
        clickSmartView.setOnClickListener(this);

        if (curSmartUserId == mUserSelfId) {
            // 1.当前小视频是自己的画面

            // 如果是采用Java视频采集，则需要设置Surface的CallBack
            if (AnyChatCoreSDK
                    .GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER) == AnyChatDefine.VIDEOCAP_DRIVER_JAVA) {
                mSurfaceLocal.getHolder().addCallback(AnyChatCoreSDK.mCameraHelper);
            }

            // 如果是采用Java视频显示，则需要设置Surface的CallBack
            if (AnyChatCoreSDK
                    .GetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_DRIVERCTRL) == AnyChatDefine.VIDEOSHOW_DRIVER_JAVA) {
                int index = mAnyChatSDK.mVideoHelper.bindVideo(clickSmartView
                        .getHolder());
                mAnyChatSDK.mVideoHelper.SetVideoUser(index, curBigUserId);
            }

            //改变当前小视频视图的参数
            clickSmartView.setTag(curBigUserId);

            //改变当前大视频视图的参数
            mSurfaceLocal.setTag(mUserSelfId);

            //开启自己的音视频的传输
            mAnyChatSDK.UserCameraControl(-1, 1);
            mAnyChatSDK.UserSpeakControl(-1, 1);

            //开启对方的音视频的传输
            mAnyChatSDK.UserCameraControl(curBigUserId, 1);
            mAnyChatSDK.UserSpeakControl(curBigUserId, 1);

        } else {
            // 2.当前的小视频画面不是自己
            // 判断两种情况
            // 2.1.大视频画面是自己的画面
            // 2.2.大视频画面是其他用户的画面

            if (curBigUserId == mUserSelfId) {
                //2.1 大视频画面是自己的画面  一开始无论点击哪里都是走这一步

                // 如果是采用Java视频采集，则需要设置Surface的CallBack  播放自己摄像头的
                if (AnyChatCoreSDK
                        .GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER) == AnyChatDefine.VIDEOCAP_DRIVER_JAVA) {
                    clickSmartView.getHolder().addCallback(AnyChatCoreSDK.mCameraHelper);
                }

                // 如果是采用Java视频显示，则需要设置Surface的CallBack 其他用户都是设置这个
                if (AnyChatCoreSDK
                        .GetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_DRIVERCTRL) == AnyChatDefine.VIDEOSHOW_DRIVER_JAVA) {
                    int index = mAnyChatSDK.mVideoHelper.bindVideo(mSurfaceLocal
                            .getHolder());
                    mAnyChatSDK.mVideoHelper.SetVideoUser(index, curSmartUserId);
                }

                //改变当前小视频视图的参数
                clickSmartView.setTag(mUserSelfId);

                //改变当前大视频视图的参数
                mSurfaceLocal.setTag(curSmartUserId);

                //开启自己的音视频的传输
                mAnyChatSDK.UserCameraControl(-1, 1);
                mAnyChatSDK.UserSpeakControl(-1, 1);

                //开启对方的音视频的传输
                mAnyChatSDK.UserCameraControl(curSmartUserId, 1);
                mAnyChatSDK.UserSpeakControl(curSmartUserId, 1);
            } else {
                //2.2 大视频画面是其他用户的画面
                // 如果是采用Java视频显示，则需要设置Surface的CallBack 其他用户都是设置这个
                if (AnyChatCoreSDK
                        .GetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_DRIVERCTRL) == AnyChatDefine.VIDEOSHOW_DRIVER_JAVA) {
                    int index = mAnyChatSDK.mVideoHelper.bindVideo(clickSmartView
                            .getHolder());
                    mAnyChatSDK.mVideoHelper.SetVideoUser(index, curBigUserId);
                }

                // 如果是采用Java视频显示，则需要设置Surface的CallBack 其他用户都是设置这个
                if (AnyChatCoreSDK
                        .GetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_DRIVERCTRL) == AnyChatDefine.VIDEOSHOW_DRIVER_JAVA) {
                    int index = mAnyChatSDK.mVideoHelper.bindVideo(mSurfaceLocal
                            .getHolder());
                    mAnyChatSDK.mVideoHelper.SetVideoUser(index, curSmartUserId);
                }
                ;
                //改变当前小视频视图的参数
                clickSmartView.setTag(curBigUserId);

                //改变当前大视频视图的参数
                mSurfaceLocal.setTag(curSmartUserId);

                //开启凯队的音视频的传输
                mAnyChatSDK.UserCameraControl(curBigUserId, 1);
                mAnyChatSDK.UserSpeakControl(curBigUserId, 1);

                //开启对方的音视频的传输
                mAnyChatSDK.UserCameraControl(curSmartUserId, 1);
                mAnyChatSDK.UserSpeakControl(curSmartUserId, 1);
            }
        }
        //设置SurfaceLocal控件是不是置于最上面
        mSurfaceLocal.setZOrderOnTop(false);
        clickSmartView.setZOrderOnTop(true);

        //获取系统的宽高设置给小视频的宽高
        int width = DisplayUtil.getScreenWidth() / 3;
        int heigth = DisplayUtil.getScreenHeight() / 3;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, heigth);
        lp.rightMargin = DisplayUtil.dp2px(10);

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
        //添加大视频
        mVideoMainLayout.addView(mSurfaceLocal, layoutParams);
        //添加小视频
        llVideoControl.addView(clickSmartView, position, lp);

        //移除视频界面的全部功能按钮
        mVideoMainLayout.removeView(mLlControl);
        ConstraintLayout.LayoutParams llControlParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        llControlParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        llControlParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        llControlParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        llControlParams.bottomMargin = DisplayUtil.dp2px(50);
        mVideoMainLayout.addView(mLlControl, llControlParams);
    }

    /**
     * 停止录制
     */
    private void stopRecord() {
        int flag = AnyChatDefine.ANYCHAT_RECORD_FLAGS_VIDEO + AnyChatDefine.ANYCHAT_RECORD_FLAGS_AUDIO;
        //停止录制
        mAnyChatSDK.StreamRecordCtrlEx(-1, 0, flag, 12306, "");

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

    /**
     * 开启录制
     */
    private void startRecord() {
        chronometer.setVisibility(View.GONE);//录制计时器显示
        ivRecordState.setVisibility(View.GONE);//正在录制图标显示
        int flag = AnyChatDefine.ANYCHAT_RECORD_FLAGS_VIDEO + AnyChatDefine.ANYCHAT_RECORD_FLAGS_AUDIO + AnyChatDefine.ANYCHAT_RECORD_FLAGS_ABREAST + AnyChatDefine.ANYCHAT_RECORD_FLAGS_STEREO;
        //开始录制 1 为开始 0 为停止
        int state = mAnyChatSDK.StreamRecordCtrlEx(-1, 1, flag, 12306, "");
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
        mOnlineUser = mAnyChatSDK.GetOnlineUser();//更新一下在线人数
        for (int i = 0; i < mOnlineUser.length; i++) {
            //打开其他用户的音视频
            mAnyChatSDK.UserCameraControl(mOnlineUser[i], 1);
            mAnyChatSDK.UserSpeakControl(mOnlineUser[i], 1);

            // 如果是采用Java视频显示，则需要设置Surface的CallBack
            SurfaceView surfaceViewRemote = (SurfaceView) llVideoControl.getChildAt(i);
            if (AnyChatCoreSDK
                    .GetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_DRIVERCTRL) == AnyChatDefine.VIDEOSHOW_DRIVER_JAVA) {
                int index = mAnyChatSDK.mVideoHelper.bindVideo(surfaceViewRemote
                        .getHolder());
                mAnyChatSDK.mVideoHelper.SetVideoUser(index, mOnlineUser[i]);
            }
        }

        mAnyChatSDK.UserCameraControl(-1, 1);//-1表示对本地视频进行控制，打开本地视频
        mAnyChatSDK.UserSpeakControl(-1, 1);//-1表示对本地音频进行控制，打开本地视频
    }

    @Override
    protected void onPause() {
        super.onPause();
        //用户切出去的时候走这个方法
        // 这时候要对用户的音视频进行关闭操作
        mOnlineUser = mAnyChatSDK.GetOnlineUser();//更新一下在线人数
        for (int i = 0; i < mOnlineUser.length; i++) {
            //关闭其他用户的音视频
            mAnyChatSDK.UserCameraControl(mOnlineUser[i], 0);
            mAnyChatSDK.UserSpeakControl(mOnlineUser[i], 0);
        }

        mAnyChatSDK.UserCameraControl(-1, 0);//-1表示对本地视频进行控制，关闭本地视频
        mAnyChatSDK.UserSpeakControl(-1, 0);//-1表示对本地音频进行控制，关闭本地视频

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(tag, "onDestroy");
        llVideoControl.removeAllViews();
        mAnyChatSDK.UserCameraControl(-1, 0);// -1表示对本地视频进行控制，关闭本地视频
        mAnyChatSDK.UserSpeakControl(-1, 0);// -1表示对本地音频进行控制，关闭本地音频
        mAnyChatSDK.removeEvent(this);
        mAnyChatSDK.LeaveRoom(-1);
        mAnyChatSDK.Logout();
        mAnyChatSDK.mSensorHelper.DestroySensor();

        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;//防止内存泄露
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
            Log.i(tag, "进来一个用户 用户名为" + mAnyChatSDK.GetUserName(dwUserId));
            userList.put(Math.abs(dwUserId), Math.abs(dwUserId));//把用户的id存进数组
            createOtherVideoView(dwUserId);//创建一个用户视图
        } else {
            //退出房间
            Log.i(tag, "退出一个用户 ID为" + dwUserId);
            Log.i(tag, "退出一个用户 用户名为" + mAnyChatSDK.GetUserName(dwUserId));
            mAnyChatSDK.UserCameraControl(dwUserId, 0);
            mAnyChatSDK.UserSpeakControl(dwUserId, 0);
            if (dwUserId != mUserSelfId) { //第一次进来会走到这个方法，需要做个判断，防止空指针
                Log.i(tag, "退出一个用户 ID索引" + userList.indexOfValue(Math.abs(dwUserId)));
                Log.i(tag, "退出一个用户 用户名为" + mAnyChatSDK.GetUserName(dwUserId));

                llVideoControl.removeViewAt(userList.indexOfValue(Math.abs(dwUserId)));//根据退出的用户id查出索引移除view
                userList.removeAt(userList.indexOfValue(Math.abs(dwUserId)));//移除索引对应的数值

            }
            ToastUtil.show("用户" + dwUserId + "离开房间");
        }
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
     * @param dwUserId    被拍照用户 ID
     * @param dwErrorCode 错误代码
     * @param lpFileName  文件保存路径
     * @param dwFlags     拍照标志
     * @param dwParam     用户自定义参数，整型
     * @param lpUserStr   用户自定义参数，字符串类型
     */
    @Override
    public void OnAnyChatSnapShotEvent(int dwUserId, int dwErrorCode, String lpFileName, int dwFlags, int dwParam, String lpUserStr) {
        Log.i(tag, "被拍照用户 ID" + dwUserId);
        Log.i(tag, "文件保存路径" + lpFileName);
        Log.i(tag, "拍照标志" + dwFlags);
        Log.i(tag, "用户自定义参数，整型" + dwParam);
        Log.i(tag, "用户自定义参数，字符串类型" + lpUserStr);

    }

    /**
     * 收到文件传输数据事件
     *
     * @param dwUserid     用户 ID，指示发送用户
     * @param FileName     文件名（含扩展名，不含路径）
     * @param TempFilePath 接收完成后，SDK 保存在本地的临时文件（包含完整路径）
     * @param dwFileLength 文件总长度
     * @param wParam       附带参数 1
     * @param lParam       附带参数 2
     * @param dwTaskId     该文件所对应的任务编号
     */
    @Override
    public void OnAnyChatTransFile(int dwUserid, String FileName, String TempFilePath, int dwFileLength, int wParam, int lParam, int dwTaskId) {
        Log.i(tag, "用户 ID，指示发送用户" + dwUserid);
        Log.i(tag, "文件名（含扩展名，不含路径）" + FileName);
        Log.i(tag, "接收完成后，SDK 保存在本地的临时文件（包含完整路径）" + TempFilePath);
        Log.i(tag, "文件总长度" + dwFileLength);
        Log.i(tag, "附带参数 1" + wParam);
        Log.i(tag, "附带参数 2" + lParam);
        Log.i(tag, "该文件所对应的任务编号" + dwTaskId);
    }

    @Override
    public void OnAnyChatTransBuffer(int dwUserid, byte[] lpBuf, int dwLen) {

    }

    @Override
    public void OnAnyChatTransBufferEx(int dwUserid, byte[] lpBuf, int dwLen, int wparam, int lparam, int taskid) {

    }

    @Override
    public void OnAnyChatSDKFilterData(byte[] lpBuf, int dwLen) {

    }
}

