package com.chinalwb.are.styles.toolitems.styles;

import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.Constants;
import com.chinalwb.are.Util;
import com.chinalwb.are.spans.AreListSpan;
import com.chinalwb.are.styles.ARE_ABS_FreeStyle;

/**
 * Created by hazel G. on 11/26/18.
 */

public class ARE_Style_IndentRight extends ARE_ABS_FreeStyle {
    private final EditText mEditText;
    private final ImageView mIndentRightImageView;

    public ARE_Style_IndentRight(AREditText editText, ImageView imageView) {
        super(editText.getContext());
        mEditText = editText;
        mIndentRightImageView = imageView;
        setListenerForImageView(imageView);
    }

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
                        span.setDepth(span.getDepth() + 1);
                    }
                }

                ARE_Style_ListNumber.reNumberInsideListItemSpans(mEditText, selectionLines[0], selectionLines[1]);
                ARE_Style_ListNumber.reNumberBehindListItemSpans(mEditText, selectionLines[1] + 1);

                for (int line = selectionLines[0]; line <= selectionLines[1]; ++line) {
                    int lineStart = Util.getThisLineStart(mEditText, line);
                    // -- Change the content to trigger the editable redraw
                    editable.insert(lineStart, Constants.ZERO_WIDTH_SPACE_STR);
                    editable.delete(lineStart + 1, lineStart + 1);
                    // -- End: Change the content to trigger the editable redraw
                }
            }
        });
    }

    @Override
    public void applyStyle(Editable editable, int start, int end) {

    }

    @Override
    public ImageView getImageView() {
        return mIndentRightImageView;
    }

    @Override
    public void setChecked(boolean isChecked) {
    }
}
