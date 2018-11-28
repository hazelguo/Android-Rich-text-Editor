package com.chinalwb.are.styles.toolitems;

import android.view.View;

import com.chinalwb.are.styles.IARE_Style;

/**
 * The default tool item check status updater.
 */
public class ARE_ToolItem_UpdaterDefault implements IARE_ToolItem_Updater {

    private IARE_ToolItem mToolItem;

    private static final int sCheckedColor = 0XffFF4081;

    private static final int sUncheckedColor = 0X00000000;

    public ARE_ToolItem_UpdaterDefault(IARE_ToolItem toolItem) {
        mToolItem = toolItem;
    }

    @Override
    public void onCheckStatusUpdate(boolean checked) {
        IARE_Style areStyle = mToolItem.getStyle();
        areStyle.setChecked(checked);
        View view = mToolItem.getView(null);
        int color = checked ? sCheckedColor : sUncheckedColor;
        view.setBackgroundColor(color);
    }

    @Override
    public void onCheckStatusUpdate(boolean oldChecked, boolean newChecked) {
        onCheckStatusUpdate(newChecked);
    }
}
