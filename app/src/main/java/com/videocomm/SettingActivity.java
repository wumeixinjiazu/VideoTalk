package com.videocomm;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.videocomm.utils.SpUtil;
import com.videocomm.utils.ToastUtil;

/**
 * @author[wengCJ]
 * @version[创建日期，2019/12/16 0016]
 * @function[功能简介 登陆界面中的设置界面Activity]
 **/

public class SettingActivity extends AbsActivity implements View.OnClickListener {

    private TextView tvSdkVersion;
    private EditText etAppId;
    private EditText etServerAddress;
    private EditText etServerPort;
    private String mAppid;
    private String mServerAddr;
    private String mServerPort;

    private String[] mVideoRecordWayList = {"本地录制", "服务器录制"};
    private String[] mVideoRecordTypeList = {"音视频", "音频", "视频"};
    private String[] mVideoRecordObjectList = {"单人", "双人", "全部"};
    private String[] mVideoRecordModeList = {"画中画模式", "并列模式", "三画面模式"};


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initTitle();//初始化标题栏
        initView();
    }

    private void initTitle() {
        TextView tvTitleTitle = findViewById(R.id.tv_title_title);
        TextView tvTitleLeft = findViewById(R.id.tv_title_left);
        tvTitleTitle.setText(getString(R.string.login_setting));
        tvTitleLeft.setOnClickListener(this);

    }

    private void initView() {
        //获取上次保存的数据
        mAppid = SpUtil.getInstance().getAppid();
        mServerAddr = SpUtil.getInstance().getServerAddr();
        mServerPort = SpUtil.getInstance().getServerPort();

        tvSdkVersion = findViewById(R.id.tv_sdk_version);
        etAppId = findViewById(R.id.et_app_id);
        etServerAddress = findViewById(R.id.et_server_address);
        etServerPort = findViewById(R.id.et_server_port);

        Spinner spinnerRecordWay = findViewById(R.id.spinner_record_way);
        Spinner spinnerRecordType = findViewById(R.id.spinner_record_type);
        Spinner spinnerRecordObject = findViewById(R.id.spinner_record_object);
        Spinner spinnerRecordMode = findViewById(R.id.spinner_record_mode);
        Spinner spinnerRecordUser = findViewById(R.id.spinner_record_user);

        Button btnSaveSetting = findViewById(R.id.btn_save_setting);
        btnSaveSetting.setOnClickListener(this);

        //初始化数值
        etAppId.setText((mAppid.length() > 0) ? mAppid : getString(R.string.default_appid));
        etServerAddress.setText((mServerAddr.length() > 0) ? mServerAddr : getString(R.string.default_server_addr));
        etServerPort.setText((mServerPort.length() > 0) ? mServerPort : getString(R.string.default_server_port));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_title_left://返回
                finish();
                break;
            case R.id.btn_save_setting://保存设置
                checkAndSaveText();
                break;
            default:
                break;

        }
    }

    private void checkAndSaveText() {
        String appId = etAppId.getText().toString();
        String serverAddress = etServerAddress.getText().toString();
        String serverPort = etServerPort.getText().toString();

        if (TextUtils.isEmpty(appId)) {
            ToastUtil.show(getString(R.string.tip_appid_not_null));
            return;
        }
        if (TextUtils.isEmpty(serverAddress)) {
            ToastUtil.show(getString(R.string.tip_server_address_not_null));
            return;
        }
        if (TextUtils.isEmpty(serverPort)) {
            ToastUtil.show(getString(R.string.tip_server_port_not_null));
            return;
        }

        SpUtil.getInstance().saveAppId(appId);
        SpUtil.getInstance().saveServerAddr(serverAddress);
        SpUtil.getInstance().saveServerPort(serverPort);


        ToastUtil.show("保存成功");
    }
}
