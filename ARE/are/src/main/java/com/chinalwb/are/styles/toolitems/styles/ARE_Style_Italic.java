package com.chinalwb.are.styles.toolitems.styles;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.spans.AreItalicSpan;
import com.chinalwb.are.styles.ARE_ABS_Style;
import com.chinalwb.are.styles.toolitems.IARE_ToolItem_Updater;

public class ARE_Style_Italic extends ARE_ABS_Style<AreItalicSpan> {

    public ARE_Style_Italic(AREditText editText, ImageView italicImage, IARE_ToolItem_Updater checkUpdater) {
        super(editText, checkUpdater);
        setListenerForImageView(italicImage);
    }

    public void setEditText(AREditText editText) {
        this.mEditText = editText;
    }

    @Override
    public void setListenerForImageView(final ImageView imageView) {
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCheckStatus(!mButtonChecked);
                if (null != mEditText) {
                    applyStyle(mEditText.getEditableText(), mEditText.getSelectionStart(), mEditText.getSelectionEnd());
                }
            }
        });
    }

    @Override
    public AreItalicSpan newSpan() {
        return new AreItalicSpan();
    }
}
