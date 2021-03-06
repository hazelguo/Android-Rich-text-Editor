package com.chinalwb.are.styles.toolitems;

import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.R;
import com.chinalwb.are.spans.ListBulletSpan;
import com.chinalwb.are.styles.ButtonCheckStatusUtil;
import com.chinalwb.are.styles.IARE_Style;
import com.chinalwb.are.styles.toolitems.styles.ARE_Style_ListBullet;

/**
 * Created by wliu on 13/08/2018.
 */

public class ARE_ToolItem_ListBullet extends ARE_ToolItem_Abstract {
    @Override
    protected IARE_ToolItem_Updater getUpdater() {
        return new ARE_ToolItem_UpdaterList(this, ARE_ToolItem_ListNumber.class);
    }

    @Override
    protected IARE_Style getStyle(AREditText editText, ImageView imageView, @Nullable IARE_ToolItem_Updater updater) {
        return new ARE_Style_ListBullet(editText, imageView, updater);
    }

    @Override
    protected int getIconDrawableRes() {
        return R.drawable.listbullet;
    }

    @Override
    public void onSelectionChanged(int selStart, int selEnd) {
        mToolItemUpdater.onCheckStatusUpdate(ButtonCheckStatusUtil.shouldCheckButton(getEditText(), ListBulletSpan.class));
    }
}
