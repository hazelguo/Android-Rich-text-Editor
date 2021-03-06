package com.chinalwb.are.styles.toolitems.styles;

import android.text.Editable;
import android.view.View;
import android.widget.ImageView;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.Constants;
import com.chinalwb.are.Util;
import com.chinalwb.are.spans.AreListSpan;
import com.chinalwb.are.styles.ARE_ABS_FreeStyle;

/**
 * Created by hazel G. on 11/26/18.
 */

public abstract class ARE_Style_Indent extends ARE_ABS_FreeStyle {

    public ARE_Style_Indent(AREditText editText, ImageView imageView) {
        super(editText, null);
        setListenerForImageView(imageView);
    }

    protected abstract int getChangeDirection();

    /**
     * 1. Increase the depth of all AreListSpans in the current selection
     * 2. ReNumber ListNumberSpans inside the current selection.
     * 3. ReNumber ListNumberSpans after the current selection.
     */
    @Override
    public void setListenerForImageView(ImageView imageView) {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable editable = mEditText.getText();
                int[] selectionLines = Util.getCurrentSelectionLines(mEditText);

                for (int line = selectionLines[0]; line <= selectionLines[1]; ++line) {
                    AreListSpan[] spans = Util.getListSpanForLine(mEditText, editable, line);
                    for (AreListSpan span : spans) {
                        span.setDepth(span.getDepth() + getChangeDirection());
                    }
                }

                ARE_Style_ListNumber.reNumberInsideListItemSpans(mEditText, selectionLines[0], selectionLines[1]);
                ARE_Style_ListNumber.reNumberBehindListItemSpansForLine(mEditText, selectionLines[1]);

                Util.triggerEditableRedraw(mEditText, editable, selectionLines);
            }
        });
    }

    @Override
    public void applyStyle(Editable editable, int start, int end) {

    }
}
