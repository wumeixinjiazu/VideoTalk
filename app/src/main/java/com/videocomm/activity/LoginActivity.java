package com.videocomm.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.AnyChatDefine;
import com.videocomm.R;
import com.videocomm.utils.PermissionUtil;
import com.videocomm.utils.SpUtil;
import com.videocomm.utils.ToastUtil;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * @author[wengCJ]
 * @version[创建日期，2019/12/16 0016]
 * @function[功能简介 登陆功能Actvity]
 **/

public class LoginActivity extends EventActivity implements View.OnClickListener {

    /**
     * app需要用到的动态添加权限 6.0以上才会去申请
     */
    String[] permissions = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    /**
     * 用户名
     */
    private EditText etUser;

    /**
     * 房间号
     */
    private EditText etRoom;

    /**
     * app的版本号
     */
    private TextView tvAppVersion;

    /**
     * 加入按钮
     */
    private Button btnJoinIn;

    /**
     * 用户名
     */
    private String mUsername;

    /**
     * 房间名
     */
    private String mRoom;

    /**
     * VideoActivity退出的请求码
     */
    private static final int VIDEOACTIVITY_EXIT_CODE = 10000;

    /**
     * 本地视频自动旋转控制
     */
    private final int LOCALVIDEOAUTOROTATION = 1;

    /**
     * 类名
     */
    private String tag = this.getClass().getSimpleName();

    /**
     * 登陆的Dialog
     */
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        InitSDK();
    }

    /**
     * 初始化SDK
     */
    private void InitSDK() {
        mAnyChatSDK.InitSDK(android.os.Build.VERSION.SDK_INT, 0);
        AnyChatCoreSDK.SetSDKOptionInt(
                AnyChatDefine.BRAC_SO_LOCALVIDEO_AUTOROTATION,
                LOCALVIDEOAUTOROTATION);
    }

    /**
     * 初始化布局
     */
    private void initView() {
        //获取上次保存的用户名房间号
        mUsername = SpUtil.getInstance().getUsername();
        mRoom = SpUtil.getInstance().getRoom();

        ImageButton ibSetting = findViewById(R.id.ib_setting);
        etUser = findViewById(R.id.et_user);
        etRoom = findViewById(R.id.et_room);
        tvAppVersion = findViewById(R.id.tv_app_version);
        btnJoinIn = findViewById(R.id.btn_join_in);

        ibSetting.setOnClickListener(this);
        btnJoinIn.setOnClickListener(this);

        //初始化用户名 房间号
        etUser.setText(mUsername);
        etRoom.setText(mRoom);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_setting://设置
                startActivity(new Intent(LoginActivity.this, SettingActivity.class));
                break;
            case R.id.btn_join_in://加入
                SpUtil.getInstance().saveUserName(etUser.getText().toString());
                SpUtil.getInstance().saveRoom(etRoom.getText().toString());

                //检查输入文本和权限
                checkTextAndPermission();
                break;
            default:
                break;
        }
    }

    /**
     * 开启登陆提示框
     */
    private void showLoginDialog() {
        if (mDialog == null) {
            mDialog = new ProgressDialog(this);
            mDialog.setMessage("登录中，请等待");
            mDialog.setCancelable(false);
        }
        mDialog.show();
    }

    /**
     * 开始连接登陆SDK
     */
    private void startLoginSDK() {
        AnyChatCoreSDK.SetSDKOptionString(AnyChatDefine.BRAC_SO_CLOUD_APPGUID, "fbe957d1-c25a-4992-9e75-d993294a5d56");
        mAnyChatSDK.Connect("cloud.anychat.cn", 8906);
        mAnyChatSDK.Login(etUser.getText().toString(), "");
    }

    /**
     * 检查输入的格式是否正确 请求权限
     */
    private void checkTextAndPermission() {
        if (TextUtils.isEmpty(etUser.getText().toString())) {
            ToastUtil.show(getString(R.string.empty_user));
            return;
        }
        if (TextUtils.isEmpty(etRoom.getText().toString())) {
            ToastUtil.show(getString(R.string.empty_room));
            return;
        }
        //请求权限 同意才能下一步
        if (PermissionUtil.checkPermission(LoginActivity.this, permissions)) {
            //展示dialog
            showLoginDialog();
            //开始登录SDK
            startLoginSDK();
        }
    }

    /**
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionUtil.REQUEST_PERMISSION_CODR
                && grantResults.length == this.permissions.length
                && grantResults[0] == PERMISSION_GRANTED
                && grantResults[1] == PERMISSION_GRANTED
                && grantResults[2] == PERMISSION_GRANTED
                && grantResults[3] == PERMISSION_GRANTED) {
            //全部请求成功后 不再走这里
            //展示dialog
            showLoginDialog();
            //开始登录SDK
            startLoginSDK();

        } else {
            //某个权限拒绝
            for (int i = 0; i < permissions.length; i++) {
                Log.i("onRequestPermission", "授权的权限: " + permissions[i]);
                Log.i("onRequestPermission", "授权的结果: " + grantResults[i]);
                //判断用户拒绝权限是是否勾选don't ask again选项，若勾选需要客户手动打开权限
                if (!ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissions[i])) {
                    showWaringDialog();
                }
            }
        }
    }

    /**
     * 提示用户自行打开权限
     */
    private void showWaringDialog() {
        new AlertDialog.Builder(this)
                .setTitle("警告！")
                .setMessage("请前往设置->应用->VideoTalk->权限中打开相关权限，否则功能无法正常运行！")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }

                }).show();
    }

    /**
     * 启动VideoActivity
     */
    private void startVideoActvity() {

        //启动视频通话
        Intent intent = new Intent(LoginActivity.this, VideoActivity.class);
        startActivityForResult(intent, VIDEOACTIVITY_EXIT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case VIDEOACTIVITY_EXIT_CODE:
                //VideoActivity退出后 要重新设置事件 不然收不到消息
                mAnyChatSDK.removeEvent(this);
                mAnyChatSDK.SetBaseEvent(this);
                break;
            default:
                break;
        }
    }

    @Override
    public void OnAnyChatLoginMessage(int dwUserId, int dwErrorCode) {
        //记录自己的userId
        if (dwErrorCode == 0) {
            int sHourseID = Integer.valueOf(etRoom.getEditableText()
                    .toString());
            mAnyChatSDK.EnterRoom(sHourseID, "");//进入房间
        } else {
            ToastUtil.show("登陆失败");
        }
    }

    @Override
    public void OnAnyChatOnlineUserMessage(int dwUserNum, int dwRoomId) {
        Log.i(tag, "房间在线用户消息 dwUserNum表示在线用户数（包含自己）:" + dwUserNum + "-dwRoomId表示房间ID:" + dwRoomId);

        //关闭Dialog
        if (mDialog != null) {
            mDialog.dismiss();
        }
        //打开视频通讯
        startVideoActvity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAnyChatSDK.removeEvent(this);
        mAnyChatSDK.Release();
    }
}
