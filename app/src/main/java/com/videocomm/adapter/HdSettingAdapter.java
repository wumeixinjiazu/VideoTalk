package com.videocomm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import com.videocomm.R;

/**
 * @author[wengCJ]
 * @version[创建日期，2019/12/16 0016]
 * @function[功能简介 视频通讯中画面设置的数据的Adapter]
 **/

public class HdSettingAdapter extends BaseAdapter {

    private String[] datas;
    private Context mContext;
    private final LayoutInflater inflater;

    private int ChooseNumber = 0;

    public HdSettingAdapter(Context context, String[] datas, int chooseNumber) {
        this.datas = datas;
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        ChooseNumber = chooseNumber;
    }

    @Override
    public int getCount() {
        return datas.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public int getChooseNumber() {
        return ChooseNumber;
    }

    public void setChooseNumber(int ChooseNumber) {
        this.ChooseNumber = ChooseNumber;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.item_hd_list, null);
        CheckedTextView ctv = view.findViewById(R.id.ctv);
        ctv.setText(datas[position]);
        if (ChooseNumber == position) {
            ctv.setChecked(true);
        }
        return view;
    }
}
