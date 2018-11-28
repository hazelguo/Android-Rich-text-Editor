package com.chinalwb.are.styles.toolitems.styles;

import android.widget.EditText;
import android.widget.ImageView;

import com.chinalwb.are.spans.AreBoldSpan;
import com.chinalwb.are.styles.ARE_ABS_Style;
import com.chinalwb.are.styles.toolitems.IARE_ToolItem_Updater;

public class ARE_Style_Bold extends ARE_ABS_Style<AreBoldSpan> {

    public ARE_Style_Bold(EditText editText, ImageView imageView, IARE_ToolItem_Updater checkUpdater) {
        super(editText, imageView, checkUpdater);
    }

    @Override
    public AreBoldSpan newSpan() {
        return new AreBoldSpan();
    }
}
