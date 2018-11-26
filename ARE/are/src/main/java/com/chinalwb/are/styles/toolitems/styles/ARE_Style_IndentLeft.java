package com.chinalwb.are.styles.toolitems.styles;

import android.widget.ImageView;

import com.chinalwb.are.AREditText;

/**
 * Created by hazel G. on 11/26/18.
 */

public class ARE_Style_IndentLeft extends ARE_Style_Indent {
    public ARE_Style_IndentLeft(AREditText editText, ImageView imageView) {
        super(editText, imageView);
    }

    @Override
    protected int getChangeDirection() {
        return -1;
    }
}
