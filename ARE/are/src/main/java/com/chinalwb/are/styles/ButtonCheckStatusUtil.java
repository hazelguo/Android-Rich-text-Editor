package com.chinalwb.are.styles;

import android.text.Editable;
import android.view.View;
import android.widget.EditText;

import com.chinalwb.are.Constants;
import com.chinalwb.are.Util;
import com.chinalwb.are.spans.AreListSpan;

public class ButtonCheckStatusUtil {
    public static boolean shouldCheckButton(EditText editText, Class type) {
        if (editText.getLayout() != null) {
            Editable editable = editText.getText();
            int[] selectionLines = Util.getCurrentSelectionLines(editText);
            for (int line = selectionLines[0]; line <= selectionLines[1]; ++line) {
                int lineStart = Util.getThisLineStart(editText, line);
                int lineEnd = Util.getThisLineEnd(editText, line);
                int span = editable.nextSpanTransition(lineStart - 1, lineEnd, type);
                if (span >= lineEnd) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * @return true if the two indent buttons should be shown. meaning the selection contains list
     *              span (ListNumberSpan or ListBulletSpan) only
     */
    public static boolean shouldEnableIndentButtons(EditText editText) {
        if (editText.getLayout() == null) {
            return false;
        }

        Editable editable = editText.getText();
        int[] selectionLines = Util.getCurrentSelectionLines(editText);
        for (int line = selectionLines[0]; line <= selectionLines[1]; ++line) {
            int lineStart = Util.getThisLineStart(editText, line);
            int lineEnd = Util.getThisLineEnd(editText, line);
            int nextSpanStart = editable.nextSpanTransition(lineStart - 1, lineEnd, AreListSpan.class);
            if (nextSpanStart >= lineEnd) {
                return false;
            }
        }
        return true;
    }

    /**
     * Updates the check status.
     */
    public static void updateCheckStatus(IARE_Style areStyle, boolean checked) {
        areStyle.setChecked(checked);
        View imageView = areStyle.getImageView();
        int color = checked ? Constants.CHECKED_COLOR : Constants.UNCHECKED_COLOR;
        imageView.setBackgroundColor(color);
    }
}
