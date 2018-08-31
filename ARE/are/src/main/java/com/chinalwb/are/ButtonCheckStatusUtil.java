package com.chinalwb.are;

import android.text.Editable;
import android.widget.EditText;

import com.chinalwb.are.spans.ListBulletSpan;

public class ButtonCheckStatusUtil {
    public static boolean shouldCheckListBulletButton(EditText editText) {
        if (editText.getLayout() != null) {
            Editable editable = editText.getText();
            int[] selectionLines = Util.getCurrentSelectionLines(editText);
            for (int line = selectionLines[0]; line <= selectionLines[1]; ++line) {
                int lineStart = Util.getThisLineStart(editText, line);
                int lineEnd = Util.getThisLineEnd(editText, line);
                int span = editable.nextSpanTransition(lineStart - 1, lineEnd, ListBulletSpan.class);
                if (span >= lineEnd) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
