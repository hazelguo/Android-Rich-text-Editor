package com.chinalwb.are.styles.toolitems;

import android.view.View;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.styles.ButtonCheckStatusUtil;

import java.util.List;

public class ARE_ToolItem_UpdaterList extends ARE_ToolItem_UpdaterDefault {
    private final AREditText mEditText;
    private ARE_ToolItem_IndentLeft mToolItemIndentLeft;
    private ARE_ToolItem_IndentRight mToolItemIndentRight;
    private IARE_ToolItem mContrastToolItem;

    public ARE_ToolItem_UpdaterList(IARE_ToolItem toolItem, Class clazz) {
        super(toolItem);
        List<IARE_ToolItem> toolitems = toolItem.getToolbar().getToolItems();
        for (IARE_ToolItem item : toolitems) {
            if (item instanceof ARE_ToolItem_IndentLeft) {
                mToolItemIndentLeft = (ARE_ToolItem_IndentLeft) item;
            } else if (item instanceof ARE_ToolItem_IndentRight) {
                mToolItemIndentRight = (ARE_ToolItem_IndentRight) item;
            } else if (clazz.isInstance(item)) {
                mContrastToolItem = item;
            }
        }
        mEditText = toolItem.getToolbar().getEditText();
    }

    @Override
    public void onCheckStatusUpdate(boolean oldChecked, boolean newChecked) {
        if (oldChecked ^ newChecked) {
            super.onCheckStatusUpdate(newChecked);
        }
    }

    private void updateIndentButtons(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        mToolItemIndentLeft.getView(mEditText.getContext()).setVisibility(visibility);
        mToolItemIndentRight.getView(mEditText.getContext()).setVisibility(visibility);
    }

    @Override
    public void onCheckStatusUpdate(boolean newChecked) {
        super.onCheckStatusUpdate(newChecked);
        updateIndentButtons(ButtonCheckStatusUtil.shouldEnableIndentButtons(mEditText));
        if (newChecked) {
            mContrastToolItem.getToolItemUpdater().onCheckStatusUpdate(false);
        }
    }
}
