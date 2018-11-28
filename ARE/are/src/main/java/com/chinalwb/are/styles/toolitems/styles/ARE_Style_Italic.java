package com.chinalwb.are.styles.toolitems.styles;

import android.widget.ImageView;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.spans.AreItalicSpan;
import com.chinalwb.are.styles.ARE_ABS_Style;
import com.chinalwb.are.styles.toolitems.IARE_ToolItem_Updater;

public class ARE_Style_Italic extends ARE_ABS_Style<AreItalicSpan> {

    public ARE_Style_Italic(AREditText editText, ImageView italicImage, IARE_ToolItem_Updater checkUpdater) {
        super(editText, italicImage, checkUpdater);
    }

    @Override
    public AreItalicSpan newSpan() {
        return new AreItalicSpan();
    }
}
