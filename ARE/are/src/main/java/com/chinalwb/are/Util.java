package com.chinalwb.are;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.text.Editable;
import android.text.Layout;
import android.text.Selection;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.chinalwb.are.spans.AreListSpan;

/**
 * All Rights Reserved.
 *
 * @author Wenbin Liu
 */
public class Util {

    /**
     * Toast message.
     */
    public static void toast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void log(String s) {
        Log.d("CAKE", s);
    }

    /**
     * Returns the line number of current cursor.
     */
    public static int getCurrentCursorLine(EditText editText) {
        int selectionStart = Selection.getSelectionStart(editText.getText());
        Layout layout = editText.getLayout();

        if (null == layout) {
            return -1;
        }
        if (selectionStart != -1) {
            return layout.getLineForOffset(selectionStart);
        }

        return -1;
    }

    public static int getLineForOffset(EditText editText, int offset) {
        Layout layout = editText.getLayout();

        if (null == layout) {
            return -1;
        }
        if (offset != -1) {
            return layout.getLineForOffset(offset);
        }

        return -1;
    }

    /**
     * Returns the selected area line numbers.
     */
    public static int[] getCurrentSelectionLines(EditText editText) {
        Editable editable = editText.getText();
        int selectionStart = Selection.getSelectionStart(editable);
        int selectionEnd = Selection.getSelectionEnd(editable);
        return getCurrentSelectionLines(editText, selectionStart, selectionEnd);
    }

    public static int[] getCurrentSelectionLines(EditText editText, int selectionStart, int selectionEnd) {
        Layout layout = editText.getLayout();

        int[] lines = new int[2];
        if (selectionStart != -1) {
            int startLine = layout.getLineForOffset(selectionStart);
            lines[0] = startLine;
        }

        if (selectionEnd != -1) {
            int endLine = layout.getLineForOffset(selectionEnd);
            lines[1] = endLine;
        }

        return lines;
    }

    public static int getLineCount(EditText editText) {
        Layout layout = editText.getLayout();
        return layout == null ? -1 : layout.getLineCount();
    }

    /**
     * Returns the line start position of the current line (which cursor is focusing now).
     */
    public static int getThisLineStart(EditText editText, int currentLine) {
        Layout layout = editText.getLayout();
        int start = 0;
        if (currentLine > 0) {
            start = layout.getLineStart(currentLine);
            if (start > 0) {
                String text = editText.getText().toString();
                char lastChar = text.charAt(start - 1);
                while (lastChar != '\n') {
                    if (currentLine > 0) {
                        currentLine--;
                        start = layout.getLineStart(currentLine);
                        if (start > 1) {
                            start--;
                            lastChar = text.charAt(start);
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        return start;
    }

    /**
     * Returns the line end position of the current line (which cursor is focusing now).
     */
    public static int getThisLineEnd(EditText editText, int currentLine) {
        Layout layout = editText.getLayout();
        if (-1 != currentLine) {
            return layout.getLineEnd(currentLine);
        }
        return -1;
    }

    /**
     * Gets the pixels by the given number of dp.
     */
    public static int getPixelByDp(Context context, int dp) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return ((int) (displayMetrics.density * dp + 0.5));
    }

    /**
     * Returns the screen width and height.
     */
    public static int[] getScreenWidthAndHeight(Context context) {
        Point outSize = new Point();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        display.getSize(outSize);

        int[] widthAndHeight = new int[2];
        widthAndHeight[0] = outSize.x;
        widthAndHeight[1] = outSize.y;
        return widthAndHeight;
    }

    public static void addZeroWidthSpaceStrSafe(Editable editable, int pos) {
        if (pos >= editable.length() || editable.charAt(pos) != Constants.ZERO_WIDTH_SPACE_INT) {
            editable.insert(pos, Constants.ZERO_WIDTH_SPACE_STR.toString());
        }
    }

    /**
     * Check if this is an empty span.
     * For example:
     * 1. a
     * 2.
     * <p>
     * Line 2 is empty
     */
    public static boolean isEmptyListItemSpan(CharSequence listItemSpanContent) {
        int spanLen = listItemSpanContent.length();
        return spanLen == 2;
    }

    public static AreListSpan[] getListSpanForLine(EditText editText, Editable editable, int line) {
        int lineStart = Util.getThisLineStart(editText, line);
        int lineEnd = Util.getThisLineEnd(editText, line);
        return editable.getSpans(lineStart, lineEnd, AreListSpan.class);
    }

    public static void triggerEditableRedraw(EditText editText, Editable editable, int[] selectionLines) {
        for (int line = selectionLines[0]; line <= selectionLines[1]; ++line) {
            AreListSpan[] spans = Util.getListSpanForLine(editText, editable, line);
            if (spans != null && spans.length > 0) {
                int lastSpanEnd = editable.getSpanEnd(spans[spans.length - 1]);
                // -- Change the content to trigger the editable redraw
                editable.insert(lastSpanEnd, Constants.ZERO_WIDTH_SPACE_STR);
                editable.delete(lastSpanEnd, lastSpanEnd + 1);
                // -- End: Change the content to trigger the editable redraw
            }
        }
    }
}
