package com.chinalwb.are.styles.toolitems;

import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.widget.ImageView;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.R;
import com.chinalwb.are.styles.IARE_Style;
import com.chinalwb.are.styles.toolitems.styles.ARE_Style_Italic;

/**
 * Created by wliu on 13/08/2018.
 */

public class ARE_ToolItem_Italic extends ARE_ToolItem_Abstract {
    @Override
    protected IARE_ToolItem_Updater getUpdater() {
        return new ARE_ToolItem_UpdaterDefault(this);
    }

    @Override
    protected IARE_Style getStyle(AREditText editText, ImageView imageView, @Nullable IARE_ToolItem_Updater updater) {
        return new ARE_Style_Italic(editText, imageView, updater);
    }

    @Override
    protected int getIconDrawableRes() {
        return R.drawable.italic;
    }

    @Override
    public void onSelectionChanged(int selStart, int selEnd) {
        boolean italicExists = false;

        AREditText editText = this.getEditText();
        Editable editable = editText.getEditableText();
        if (selStart > 0 && selStart == selEnd) {
            CharacterStyle[] styleSpans = editable.getSpans(selStart - 1, selStart, CharacterStyle.class);

            for (int i = 0; i < styleSpans.length; i++) {
                if (styleSpans[i] instanceof StyleSpan) {
                    if (((StyleSpan) styleSpans[i]).getStyle() == Typeface.ITALIC) {
                        italicExists = true;
                    }
                }
            }
        } else {
			//
			// Selection is a range
			CharacterStyle[] styleSpans = editable.getSpans(selStart, selEnd, CharacterStyle.class);

			for (int i = 0; i < styleSpans.length; i++) {

				if (styleSpans[i] instanceof StyleSpan) {
					if (((StyleSpan) styleSpans[i]).getStyle() == android.graphics.Typeface.ITALIC) {
						if (editable.getSpanStart(styleSpans[i]) <= selStart
								&& editable.getSpanEnd(styleSpans[i]) >= selEnd) {
							italicExists = true;
						}
					} else if (((StyleSpan) styleSpans[i]).getStyle() == android.graphics.Typeface.BOLD_ITALIC) {
						if (editable.getSpanStart(styleSpans[i]) <= selStart
								&& editable.getSpanEnd(styleSpans[i]) >= selEnd) {
							italicExists = true;
						}
					}
				}
			}
		}

        mToolItemUpdater.onCheckStatusUpdate(italicExists);
    }
}
