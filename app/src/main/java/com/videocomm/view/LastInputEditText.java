package com.videocomm.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * @author[wengCJ]
 * @version[创建日期，2020/1/3 0003]
 * @function[功能简介 保证光标始终在最后面的EditText]
 **/
public class LastInputEditText extends EditText {
    public LastInputEditText(Context context) {
        super(context);
    }

    public LastInputEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LastInputEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (selStart == selEnd) { // 防止不能多选
            setSelection(getText().length()); // 保证光标始终在最后面
        }
    }

}
