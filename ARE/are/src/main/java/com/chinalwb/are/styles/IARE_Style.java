package com.chinalwb.are.styles;

import android.text.Editable;
import android.widget.EditText;
import android.widget.ImageView;

import com.chinalwb.are.styles.toolitems.IARE_ToolItem_Updater;

public interface IARE_Style {

    /**
     * For styles like Bold / Italic / Underline, by clicking the ImageView,
     * we should change the UI, so user can notice that this style takes
     * effect now.
     */
    void setListenerForImageView(ImageView imageView);

    /**
     * Apply the style to the change start at start end at end.
     */
    void applyStyle(Editable editable, int start, int end);

    /**
     * Sets if this style is checked.
     */
    void setChecked(boolean isChecked);

    /**
     * Returns if current style is checked.
     */
    boolean getIsChecked();
}
