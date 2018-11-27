package com.chinalwb.are.events;

import android.text.Layout;
import android.text.Spannable;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * This base MovementMethod is a compound product from {@link android.text.method.ArrowKeyMovementMethod} and {@link android.text.method.LinkMovementMethod}.
 * It supports all behaviours of theirs.

 * Before sub-class extends this, you must clearly know 2 points of them:

 * {@link ArrowKeyMovementMethod}: ArrowKeyMovementMethod does support selection of text but not the clicking of links.
 * {@link LinkMovementMethod}: LinkMovementMethod does support clicking of links but not the selection of text.

 * In order to fit general and variable requirements, it should support the above 2 behaviors. So this base-class is just to solve it,
 * which compounding "support selection of text" and "support clicking of links" to one class. So in later you just
 * extends this only!

 * In addition, you'd better know this:
 *
 *     In some Samsung devices(e.g. Samsung GT-N7108, Android 4.3 version), one EditText contains links(can be clicked) and selection(can be selected):
 *     If you just extends {@link LinkMovementMethod}, it'll report some exception and result in a NullPointerException!
 *     So, this solution is pretty perfect to solve it!
 *     e.g. For solving Bug #16456 App crash if click TextView in process ADP page
 *
 * @author Created by SongHui (@BroadVision) on 2017/7/14.
 */
public class AREMovementMethod extends ArrowKeyMovementMethod {
    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        // Supports android.text.method.LinkMovementMethod.onTouchEvent(TextView, Spannable, MotionEvent)'s
        // clickable event. So post all these codes to here and comment out "Selection.removeSelection(buffer);"
        // because this has extended ArrowKeyMovementMethod which has supported Selection text.
        //
        // So, it is forbidden modifying the bellow codes!
        // ----------- Last modified by Songhui on 2017-7-14
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    link[0].onClick(widget);
                } else if (action == MotionEvent.ACTION_DOWN) {
                    android.text.Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
                }

                return true;
            }
            /*else {
                Selection.removeSelection(buffer);
            }*/
        }
        return super.onTouchEvent(widget, buffer, event);
    }
}
