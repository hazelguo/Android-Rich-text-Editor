package com.chinalwb.are.styles.toolitems;

import android.content.Context;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.Constants;
import com.chinalwb.are.R;
import com.chinalwb.are.Util;
import com.chinalwb.are.spans.ListBulletSpan;
import com.chinalwb.are.styles.IARE_Style;
import com.chinalwb.are.styles.toolitems.styles.ARE_Style_ListBullet;

/**
 * Created by wliu on 13/08/2018.
 */

public class ARE_ToolItem_ListBullet extends ARE_ToolItem_Abstract {

    @Override
    public IARE_ToolItem_Updater getToolItemUpdater() {
        if (mToolItemUpdater == null) {
            mToolItemUpdater = new ARE_ToolItem_UpdaterDefault(this, Constants.CHECKED_COLOR, Constants.UNCHECKED_COLOR);
            setToolItemUpdater(mToolItemUpdater);
        }
        return mToolItemUpdater;
    }

    @Override
    public IARE_Style getStyle() {
        if (mStyle == null) {
            AREditText editText = this.getEditText();
            IARE_ToolItem_Updater toolItemUpdater = getToolItemUpdater();
            mStyle = new ARE_Style_ListBullet(editText, (ImageView) mToolItemView, toolItemUpdater);
        }
        return mStyle;
    }

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
            imageView.setImageResource(R.drawable.listbullet);
            imageView.bringToFront();
            mToolItemView = imageView;
        }

        return mToolItemView;
    }

    @Override
    public void onSelectionChanged(int selStart, int selEnd) {
        EditText editText = getEditText();
        if (editText.getLayout() != null) {
            Editable editable = editText.getText();
            int[] selectionLines = Util.getCurrentSelectionLines(editText);
            for (int line = selectionLines[0]; line <= selectionLines[1]; ++line) {
                int lineStart = Util.getThisLineStart(editText, line);
                int lineEnd = Util.getThisLineEnd(editText, line);
                int span = editable.nextSpanTransition(lineStart - 1, lineEnd, ListBulletSpan.class);
                if (span >= lineEnd) {
                    mToolItemUpdater.onCheckStatusUpdate(false);
                    return;
                }
            }
            mToolItemUpdater.onCheckStatusUpdate(true);
        }
    }
}
