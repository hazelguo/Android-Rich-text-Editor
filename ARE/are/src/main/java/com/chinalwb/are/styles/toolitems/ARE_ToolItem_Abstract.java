package com.chinalwb.are.styles.toolitems;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.Util;
import com.chinalwb.are.styles.IARE_Style;
import com.chinalwb.are.styles.toolbar.IARE_Toolbar;

/**
 * Created by wliu on 13/08/2018.
 */

public abstract class ARE_ToolItem_Abstract implements IARE_ToolItem {

    private IARE_Style mStyle;

    private ImageView mToolItemView;

    private IARE_Toolbar mToolbar;

    protected IARE_ToolItem_Updater mToolItemUpdater;

    @Override
    public IARE_Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    public void setToolbar(IARE_Toolbar toolbar) {
        mToolbar = toolbar;
    }

    protected AREditText getEditText() {
        return mToolbar.getEditText();
    }

    public IARE_ToolItem_Updater getToolItemUpdater() {
        if (mToolItemUpdater == null) {
            mToolItemUpdater = getUpdater();
        }
        return mToolItemUpdater;
    }

    protected abstract IARE_ToolItem_Updater getUpdater();


    @Override
    public IARE_Style getStyle() {
        if (mStyle == null) {
            AREditText editText = this.getEditText();
            IARE_ToolItem_Updater toolItemUpdater = getToolItemUpdater();
            mStyle = getStyle(editText, mToolItemView, toolItemUpdater);
        }
        return mStyle;
    }

    protected abstract IARE_Style getStyle(AREditText editText,
                                           ImageView imageView,
                                           @Nullable IARE_ToolItem_Updater updater);

    @Override
    public View getView(Context context) {
        if (null == context) {
            return mToolItemView;
        }
        if (mToolItemView == null) {
            ImageView imageView = new ImageView(context);
            int size = Util.getPixelByDp(context, 40);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            imageView.setLayoutParams(params);
            imageView.setImageResource(getIconDrawableRes());
            imageView.bringToFront();
            if (!visibleByDefault()) {
                imageView.setVisibility(View.GONE);
            }
            mToolItemView = imageView;
        }

        return mToolItemView;
    }

    @DrawableRes
    protected abstract int getIconDrawableRes();

    protected boolean visibleByDefault() {
        return true;
    }

}
