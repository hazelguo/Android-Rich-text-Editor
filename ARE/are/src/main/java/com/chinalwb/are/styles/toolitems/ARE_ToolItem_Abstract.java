package com.chinalwb.are.styles.toolitems;

import android.content.Intent;
import android.view.View;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.styles.IARE_Style;
import com.chinalwb.are.styles.toolbar.IARE_Toolbar;

/**
 * Created by wliu on 13/08/2018.
 */

public abstract class ARE_ToolItem_Abstract implements IARE_ToolItem {

    IARE_Style mStyle;

    View mToolItemView;

    IARE_ToolItem_Updater mToolItemUpdater;

    private IARE_Toolbar mToolbar;

    @Override
    public IARE_Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    public void setToolbar(IARE_Toolbar toolbar) {
        mToolbar = toolbar;
    }

    @Override
    public void setToolItemUpdater(IARE_ToolItem_Updater toolItemUpdater) {
        mToolItemUpdater = toolItemUpdater;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Default do nothing
        // Children classes can override if necessary
        return;
    }

    public AREditText getEditText() {
        return mToolbar.getEditText();
    }

}
