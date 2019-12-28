package com.videocomm;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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

import com.bairuitech.anychat.AnyChatBaseEvent;
import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.AnyChatDefine;
import com.videocomm.bean.UserBean;
import com.videocomm.utils.PermissionUtil;
import com.videocomm.utils.SpUtil;
import com.videocomm.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * @author[wengCJ]
 * @version[创建日期，2019/12/16 0016]
 * @function[功能简介 登陆功能Actvity]
 **/

public class LoginActivity extends AbsActivity implements View.OnClickListener, AnyChatBaseEvent {

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
     * 自己的用户id
     */
    public static int mUserSelfId;

    private static final int VIDEOACTIVITY_EXIT_CODE = 10000;

    public AnyChatCoreSDK anyChatSDK;
    private final int LOCALVIDEOAUTOROTATION = 1; // 本地视频自动旋转控制
    private String tag = this.getClass().getSimpleName();
    private List<UserBean> mUserBeanList = new ArrayList<>();
    private ProgressDialog mDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        InitSDK();
    }

    private void InitSDK() {
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

                //检查输入文本
                checkText();
                //请求权限
//                requestPermission();

                break;
            default:
                break;
        }
    }

    // 打开系统的文件选择器
    public void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        try {
            startActivityForResult(Intent.createChooser(intent, "选择文件"), 1234);
        } catch (android.content.ActivityNotFoundException ex) {
            ToastUtil.show("没有文件管理器");
        }
//        startActivityForResult(intent, 1234);
    }

    private void showLoginDialog() {
        if (mDialog == null) {
            mDialog = new ProgressDialog(this);
            mDialog.setMessage("登录中，请等待");
            mDialog.setCancelable(false);
            mDialog.show();
        } else {
            mDialog.show();
        }

    }

    private void startLoginSDK() {
        AnyChatCoreSDK.SetSDKOptionString(AnyChatDefine.BRAC_SO_CLOUD_APPGUID, "fbe957d1-c25a-4992-9e75-d993294a5d56");
        anyChatSDK.Connect("cloud.anychat.cn", 8906);
        anyChatSDK.Login(etUser.getText().toString(), "");

    }

    /**
     * 检查输入的格式是否正确 请求权限
     */
    private void checkText() {
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

    private void startVideoActvity() {

        //启动视频通话
        Intent intent = new Intent(LoginActivity.this, VideoActivity.class);
        startActivityForResult(intent, VIDEOACTIVITY_EXIT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1234:
                if (data == null) {
                    // 用户未选择任何文件，直接返回
                    return;
                }
                Uri uri = data.getData();
                // 通过ContentProvider查询文件路径
                ContentResolver resolver = this.getContentResolver();
                Cursor cursor = resolver.query(uri, null, null, null, null);
                if (cursor == null) {
                    // 未查询到，说明为普通文件，可直接通过URI获取文件路径
                    String path = uri.getPath();
                    Log.i(tag, "c------->" + path);
                    return;
                }
                if (cursor.moveToFirst()) {
                    // 多媒体文件，从数据库中获取文件的真实路径
                    String path = cursor.getString(cursor.getColumnIndex("_data"));
                    Log.i(tag, "------->" + path);

                }
                cursor.close();
                break;
            case VIDEOACTIVITY_EXIT_CODE:
                //VideoActivity退出后 要重新设置事件 不然收不到消息
                anyChatSDK.removeEvent(this);
                anyChatSDK.SetBaseEvent(this);
                break;
            default:
                break;

        }

    }

    @Override
    public void OnAnyChatConnectMessage(boolean bSuccess) {
        if (!bSuccess) {
            ToastUtil.show("连接服务器失败，自动重连，请稍后...");
            System.out.println("connect failed");
        }

    }

    @Override
    public void OnAnyChatLoginMessage(int dwUserId, int dwErrorCode) {
        //记录自己的userId
        if (dwErrorCode == 0) {
            int sHourseID = Integer.valueOf(etRoom.getEditableText()
                    .toString());
            anyChatSDK.EnterRoom(sHourseID, "");//进入房间
            mUserSelfId = dwUserId;
        } else {
            ToastUtil.show("登陆失败");
        }

    }

    @Override
    public void OnAnyChatEnterRoomMessage(int dwRoomId, int dwErrorCode) {
        Log.i(tag, "OnAnyChatEnterRoomMessage" + dwRoomId + "err:"
                + dwErrorCode);
    }

    @Override
    public void OnAnyChatOnlineUserMessage(int dwUserNum, int dwRoomId) {
        Log.i(tag, "房间在线用户消息 dwUserNum表示在线用户数（包含自己）:" + dwUserNum + "-dwRoomId表示房间ID:" + dwRoomId);

        updateUserList();
        //关闭Dialog
        if (mDialog != null) {
            mDialog.dismiss();
        }
        //打开视频通讯
        startVideoActvity();
    }

    private void updateUserList() {
        mUserBeanList.clear();
        //添加自己的参数
        UserBean userSelfBean = new UserBean();
        userSelfBean.setUserId(mUserSelfId);
        userSelfBean.setUserName(etUser.getText().toString());
        mUserBeanList.add(userSelfBean);

        //添加其他用户的参数
        int[] userID = anyChatSDK.GetOnlineUser();//获取在线的用户id
        for (int index = 0; index < userID.length; ++index) {
            Log.i(tag, "打印房间其他的用户id" + userID[index]);
            Log.i(tag, "打印房间其他的用户名" + anyChatSDK.GetUserName(userID[index]));

            UserBean info = new UserBean();
            info.setUserName(anyChatSDK.GetUserName(userID[index]));
            info.setUserId(userID[index]);
            mUserBeanList.add(info);
        }
    }

    /**
     * 用户进入/退出房间消息，dwUserId表示用户ID号，bEnter表示该用户是进入（TRUE）或离开（FALSE）房间
     * <p>
     * 进来会先触发一次
     *
     * @param dwUserId
     * @param bEnter
     */
    @Override
    public void OnAnyChatUserAtRoomMessage(int dwUserId, boolean bEnter) {
        if (bEnter) {
            Log.i(tag, "用户进入 打印房间其他的用户id" + anyChatSDK.GetUserName(dwUserId));
            Log.i(tag, "用户进入 打印房间其他的用户名" + dwUserId);
            //用户进入
            UserBean info = new UserBean();
            info.setUserId(dwUserId);
            info.setUserName(anyChatSDK.GetUserName(dwUserId));
            mUserBeanList.add(info);
        } else {
            //用户退出
            Log.i(tag, "用户退出 打印房间其他的用户id" + anyChatSDK.GetUserName(dwUserId));
            Log.i(tag, "用户退出 打印房间其他的用户名" + dwUserId);
            for (int i = 0; i < mUserBeanList.size(); i++) {
                if (mUserBeanList.get(i).getUserId() == dwUserId) {
                    mUserBeanList.remove(i);
                }
            }
        }

        for (UserBean bean : mUserBeanList) {
            Log.i(tag, "检查用户进入退出 列表数据是否改变" + bean.getUserId());
            Log.i(tag, "检查用户进入退出 列表数据是否改变" + bean.getUserName());

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        anyChatSDK.removeEvent(this);
        anyChatSDK.Release();
    }
}
