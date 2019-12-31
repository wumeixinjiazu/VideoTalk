package com.videocomm.dlgFragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.AnyChatDefine;
import com.videocomm.R;
import com.videocomm.adapter.HdSettingAdapter;
import com.videocomm.utils.SpUtil;
import com.videocomm.utils.ToastUtil;

/**
 * @author[wengCJ]
 * @version[创建日期，2019/12/16 0016]
 * @function[功能简介 视频通讯界面中画面设置的DialogFragment]
 **/

public class HdSettingFragment extends DialogFragment implements View.OnClickListener {

    private String[] hdSettingList = new String[]{"超清 1080P", "高清 720P", "标准 480P", "流畅 320P", "自定义画质"};
    /**
     * 分辨率参数-宽
     */
    private int[] hdSettingWidth = {320, 640, 720, 1280};
    /**
     * 分辨率参数-高
     */
    private int[] hdSettingHeight = {240, 480, 480, 720};
    /**
     * 码率
     */
    private final int[] mArrVideoBitrateValue = { 100 * 1000, 400 * 1000, 600 * 1000, 800 * 1000, 200 * 1000, 300 * 1000, 500 * 1000, 800 * 1000, 1000 * 1000};
    /**
     * 帧率
     */
    private final int[] mArrVideofpsValue = {10, 15, 20, 25};


    private String[] resolutionList = new String[]{"320 x 240", "352 x 288", "640 x 480", "720 x 480", "1280 x 720", "1920 x 1280"};
    private String[] frameList = new String[]{"5 f/s", "10 f/s", "15 f/s", "20 f/s", "25 f/s", "30 f"};
    private String[] rateList = new String[]{"100 kb/s", "150 kb/s", "200 kb/s", "300 kb/s", "500 kb/s", "800 kb/s", "1 Mb/s", "1.2 Mb/s", "1.5 Mb/s", "2 Mb/s"};

    private ListView lvHdList;
    private View line;
    private TextView tvDlgClose;
    private TextView tvTitleLeft;
    private LinearLayout llCustom;

    private int hdSettingItem;
    private int mResolution;
    private int mFrame;
    private int mRate;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //获取上次设置的画质设置
        hdSettingItem = SpUtil.getInstance().getHdSettingItem();
        //获取上次设置的自定义画质
        mResolution = SpUtil.getInstance().getHdCustomResolution();
        mFrame = SpUtil.getInstance().getHdCustomFrame();
        mRate = SpUtil.getInstance().getHdCustomRate();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        View view = inflater.inflate(R.layout.dlg_hd_setting, ((ViewGroup) window.findViewById(android.R.id.content)), false);//需要用android.R.id.content这个view
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//注意此处
        window.setGravity(Gravity.BOTTOM);
        window.setLayout(-1, -2);//这2行,和上面的一样,注意顺序就行
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.windowAnimations = R.style.HdSettingAnimation;//设置DialogFragment的动画
        window.setAttributes(lp);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitleLeft = view.findViewById(R.id.tv_title_left);
        tvDlgClose = view.findViewById(R.id.tv_dlg_close);
        line = view.findViewById(R.id.line);
        lvHdList = view.findViewById(R.id.lv_hd_list);
        llCustom = view.findViewById(R.id.ll_custom);//自定义画质布局

        tvTitleLeft.setClickable(false);
        tvTitleLeft.setOnClickListener(this);
        tvDlgClose.setOnClickListener(this);

        final HdSettingAdapter adapter = new HdSettingAdapter(getContext(), hdSettingList, hdSettingItem);
        lvHdList.setAdapter(adapter);
        lvHdList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //获取上次选中的View
                View preView = lvHdList.getChildAt(adapter.getChooseNumber());
                CheckedTextView preCtv = preView.findViewById(R.id.ctv);
                preCtv.setChecked(false);
                //设置当前的View选中
                CheckedTextView curCtv = view.findViewById(R.id.ctv);
                curCtv.setChecked(true);
                //把当前选中的位置记录并保存下来
                SpUtil.getInstance().saveHdSettingItem(position);
                adapter.setChooseNumber(position);
                adapter.notifyDataSetChanged();
                if (position == adapter.getCount() - 1) {
                    //最后一个
                    lvHdList.setVisibility(View.GONE);
                    line.setVisibility(View.GONE);
                    tvTitleLeft.setText(getString(R.string.hd_setting_custom));
                    tvTitleLeft.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_back, 0, 0, 0);
                    tvTitleLeft.setClickable(true);
                    llCustom.setVisibility(View.VISIBLE);
                } else {
                    //anychat可以设置六个分辨率 我们只有四个
                    // 设置本地视频采集分辨率
                    AnyChatCoreSDK.SetSDKOptionInt(
                            AnyChatDefine.BRAC_SO_LOCALVIDEO_WIDTHCTRL, hdSettingWidth[position]);

                    AnyChatCoreSDK.SetSDKOptionInt(
                            AnyChatDefine.BRAC_SO_LOCALVIDEO_HEIGHTCTRL,
                            hdSettingHeight[position]);

                    // 设置本地视频编码的帧率
                    AnyChatCoreSDK.SetSDKOptionInt(
                            AnyChatDefine.BRAC_SO_LOCALVIDEO_FPSCTRL, 15
                    );

                    // 设置本地视频编码的码率（如果码率为0，则表示使用质量优先模式）
                    AnyChatCoreSDK.SetSDKOptionInt(
                            AnyChatDefine.BRAC_SO_LOCALVIDEO_BITRATECTRL,
                            mArrVideoBitrateValue[position]);

                    // 让视频参数生效
                    AnyChatCoreSDK.SetSDKOptionInt(
                            AnyChatDefine.BRAC_SO_LOCALVIDEO_APPLYPARAM,
                            1);

                    ToastUtil.show("像素调节成功" + position);
                }
            }
        });

        initHdCustom(view);//初始化自定义画质布局
    }

    private void initHdCustom(View view) {
        SeekBar sbResolution = view.findViewById(R.id.sb_resolution);
        SeekBar sbFrame = view.findViewById(R.id.sb_frame);
        SeekBar sbRate = view.findViewById(R.id.sb_rate);
        final TextView tvResolution = view.findViewById(R.id.tv_resolution);
        final TextView tvFrame = view.findViewById(R.id.tv_frame);
        final TextView tvRate = view.findViewById(R.id.tv_rate);

        //设置上次设置的进度值
        sbResolution.setProgress(mResolution);
        sbFrame.setProgress(mFrame);
        sbRate.setProgress(mRate);

        //设置进度条最大值
        sbResolution.setMax(resolutionList.length - 1);
        sbFrame.setMax(frameList.length - 1);
        sbRate.setMax(rateList.length - 1);

        //自定义分辨率的监听
        sbResolution.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //滑动更新进度
                tvResolution.setText(resolutionList[progress]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //滑动结束当前状态
                SpUtil.getInstance().saveHdCustomResolution(seekBar.getProgress());
            }
        });

        //自定义帧率的监听
        sbFrame.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvFrame.setText(frameList[progress]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SpUtil.getInstance().saveHdCustomFrame(seekBar.getProgress());
            }
        });

        //自定义码率的监听
        sbRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvRate.setText(rateList[progress]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SpUtil.getInstance().saveHdCustomRate(seekBar.getProgress());
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_title_left:
                lvHdList.setVisibility(View.VISIBLE);
                line.setVisibility(View.VISIBLE);
                tvTitleLeft.setText(getString(R.string.hd_setting));
                tvTitleLeft.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                tvTitleLeft.setClickable(false);
                llCustom.setVisibility(View.GONE);
                break;
            case R.id.tv_dlg_close:
                dismiss();
                break;
            default:
                break;
        }
    }


}
