package com.chinalwb.are.styles.toolitems;

import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.R;
import com.chinalwb.are.styles.IARE_Style;
import com.chinalwb.are.styles.toolitems.styles.ARE_Style_Indent;

/**
 * Created by hazel G. on 26/11/2018.
 */

public class ARE_ToolItem_IndentLeft extends ARE_ToolItem_Abstract {
    @Override
    protected IARE_ToolItem_Updater getUpdater() {
        return null;
    }

    @Override
    protected IARE_Style getStyle(AREditText editText,
                                  ImageView imageView,
                                  @Nullable IARE_ToolItem_Updater updater) {
        return new ARE_Style_Indent(editText, imageView) {
            @Override
            protected int getChangeDirection() {
                return -1;
            }
        };
    }

    @Override
    protected int getIconDrawableRes() {
        return R.drawable.indentleft;
    }

    @Override
    protected boolean visibleByDefault() {
        return false;
    }

    @Override
    public void onSelectionChanged(int selStart, int selEnd) {
    }
}
