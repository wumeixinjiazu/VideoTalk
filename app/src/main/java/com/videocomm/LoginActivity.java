package com.videocomm;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.videocomm.R;
import com.videocomm.utils.SpUtil;
import com.videocomm.utils.ToastUtil;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * @author[wengCJ]
 * @version[创建日期，2019/12/16 0016]
 * @function[功能简介 登陆功能Actvity]
 **/

public class LoginActivity extends AbsActivity implements View.OnClickListener {

    private EditText etUser;//用户名
    private EditText etRoom;//房间号
    private TextView tvAppVersion;
    private Button btnJoinIn;

    private String mUsername;
    private String mRoom;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        //获取上次保存的用户名房间号
        mUsername = SpUtil.getUsername();
        mRoom = SpUtil.getRoom();

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
                SpUtil.saveUserName(etUser.getText().toString());
                SpUtil.saveRoom(etRoom.getText().toString());
                checkText();
                break;
            default:
                break;
        }
    }

    /**
     * 检查输入的格式是否正确
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

        //请求权限
        requestPermission();
    }

    /**
     * 请求权限 照相机 和 音频
     */
    private void requestPermission() {
        String[] permissions = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO};

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(LoginActivity.this,
                    permission) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(LoginActivity.this,
                        permissions,
                        10086);
                return;
            }
        }
        //启动视频通话
        startVideoActvity();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10086
                && grantResults.length == 2
                && grantResults[0] == PERMISSION_GRANTED
                && grantResults[1] == PERMISSION_GRANTED) {
            //启动视频通话
            startVideoActvity();
        }
    }

    private void startVideoActvity() {
        //启动视频通话
        Intent intent = new Intent(LoginActivity.this, VideoActivity.class);
        intent.putExtra("user_name", etUser.getText().toString());
        intent.putExtra("room", etRoom.getText().toString());
        startActivity(intent);
    }
}
