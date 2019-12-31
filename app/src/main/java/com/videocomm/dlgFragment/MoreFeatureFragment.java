package com.videocomm.dlgFragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.AnyChatOutParam;
import com.videocomm.LoginActivity;
import com.videocomm.R;
import com.videocomm.utils.ToastUtil;

/**
 * @author[wengCJ]
 * @version[创建日期，2019/12/30 0030]
 * @function[功能简介]
 **/
public class MoreFeatureFragment extends DialogFragment implements View.OnClickListener {

    private static final int OPEN_SYSTEM_FILE_MANAGER_CODE = 1;

    private AnyChatCoreSDK anyChatSDK;
    private String tag = this.getClass().getSimpleName();

    public MoreFeatureFragment(AnyChatCoreSDK anyChatSDK) {

        this.anyChatSDK = anyChatSDK;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        View view = inflater.inflate(R.layout.dlg_more_feature, ((ViewGroup) window.findViewById(android.R.id.content)), false);//需要用android.R.id.content这个view
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//注意此处
        window.setGravity(Gravity.BOTTOM);
        window.setLayout(-1, -2);//这2行,和上面的一样,注意顺序就行
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.windowAnimations = R.style.HdSettingAnimation;//设置DialogFragment的动画
        lp.height = 500;
        window.setAttributes(lp);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton ibSnapShoot = view.findViewById(R.id.ib_snap_shoot);
        ImageButton ibUploadFile = view.findViewById(R.id.ib_upload_file);

        ibSnapShoot.setOnClickListener(this);
        ibUploadFile.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_snap_shoot://快照
                openUsersList();
                break;
            case R.id.ib_upload_file://上传文件
                chooseFile();
                break;
            default:
                break;
        }
    }

    private void openUsersList() {

        //获取在线的人数 不包括自己
        final int[] users = anyChatSDK.GetOnlineUser();

        //显示的字符数组
        String[] selectStrs = new String[users.length + 1];

        //添加自己的用户名 ID
        selectStrs[0] = "ID：" + LoginActivity.mUserSelfId + " 用户名：" + anyChatSDK.GetUserName(LoginActivity.mUserSelfId) + " (自己)";

        //添加其他用户的用户名 ID
        for (int i = 0; i < users.length; i++) {
            selectStrs[i + 1] = "ID：" + users[i] + " 用户名：" + anyChatSDK.GetUserName(users[i]);
        }

        //创建单选选择框
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
        alertBuilder.setTitle("请选择拍照的用户");

        alertBuilder.setItems(selectStrs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    anyChatSDK.SnapShot(LoginActivity.mUserSelfId, 0, 0);
                } else {
                    anyChatSDK.SnapShot(users[which - 1], 0, 0);
                }
            }
        });

        alertBuilder.show();
    }


    /**
     * 打开系统的文件选择器
     * <p>
     * intent.setType(“image/*”);
     * //intent.setType(“audio/*”); //选择音频
     * //intent.setType(“video/*”); //选择视频 （mp4 3gp 是android支持的视频格式）
     * //intent.setType(“video/*;image/*”);//同时选择视频和图片
     */
    public void chooseFile() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("file/*");
        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File"), OPEN_SYSTEM_FILE_MANAGER_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            ToastUtil.show("没有文件管理器");
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_SYSTEM_FILE_MANAGER_CODE) {
            if (data == null) {
                // 用户未选择任何文件，直接返回
                return;
            }
            Uri uri = data.getData();
            String path = uri.getPath();
            ToastUtil.show(path);
            showChooseFile(path);

        }
    }

    /**
     * 展示所选择的文件
     *
     * @param path 文件地址
     */
    private void showChooseFile(final String path) {
        new AlertDialog.Builder(getContext()).setTitle("确定要上传以下文件吗?").setMessage(path).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //选择接收的用户
                chooseReceiveUser(path);
            }
        }).show();


    }

    /**
     * 选择要发送的用户
     */
    private void chooseReceiveUser(final String path) {
        //获取在线的人数 不包括自己
        final int[] users = anyChatSDK.GetOnlineUser();

        //显示的字符数组
        String[] selectStrs = new String[users.length + 1];

        //添加自己的用户名 ID
        selectStrs[0] = "ID：" + LoginActivity.mUserSelfId + " 用户名：" + anyChatSDK.GetUserName(LoginActivity.mUserSelfId) + " (自己)";

        //添加其他用户的用户名 ID
        for (int i = 0; i < users.length; i++) {
            selectStrs[i + 1] = "ID：" + users[i] + " 用户名：" + anyChatSDK.GetUserName(users[i]);
        }

        //创建单选选择框
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
        alertBuilder.setTitle("请选择接收的用户");

        alertBuilder.setItems(selectStrs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AnyChatOutParam param = new AnyChatOutParam();
                anyChatSDK.TransFile(users[which - 1], path, 0, 0, 0, param);
                int result = param.GetIntValue();
                Log.i(tag, "发送路径" + path);
                Log.i(tag, "发送结果" + result);
            }
        });

        alertBuilder.show();


    }

}
