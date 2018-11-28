package com.chinalwb.are.styles.toolitems.styles;

import android.widget.ImageView;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.spans.AreUnderlineSpan;
import com.chinalwb.are.styles.ARE_ABS_Style;
import com.chinalwb.are.styles.toolitems.IARE_ToolItem_Updater;

public class ARE_Style_Underline extends ARE_ABS_Style<AreUnderlineSpan> {

    public ARE_Style_Underline(AREditText editText, ImageView underlineImage, IARE_ToolItem_Updater checkUpdater) {
        super(editText, underlineImage, checkUpdater);
    }

    @Override
    public AreUnderlineSpan newSpan() {
        return new AreUnderlineSpan();
    }
}
