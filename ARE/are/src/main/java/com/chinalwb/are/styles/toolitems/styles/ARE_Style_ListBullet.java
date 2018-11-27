package com.chinalwb.are.styles.toolitems.styles;

import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.styles.ButtonCheckStatusUtil;
import com.chinalwb.are.Constants;
import com.chinalwb.are.Util;
import com.chinalwb.are.spans.AreListSpan;
import com.chinalwb.are.spans.ListBulletSpan;
import com.chinalwb.are.spans.ListNumberSpan;
import com.chinalwb.are.styles.ARE_ABS_FreeStyle;
import com.chinalwb.are.styles.toolitems.IARE_ToolItem_Updater;

import static com.chinalwb.are.Util.addZeroWidthSpaceStrSafe;
import static com.chinalwb.are.Util.isEmptyListItemSpan;

/**
 * All Rights Reserved.
 *
 * @author Wenbin Liu
 */
public class ARE_Style_ListBullet extends ARE_ABS_FreeStyle {
    public ARE_Style_ListBullet(AREditText editText, ImageView imageView, IARE_ToolItem_Updater checkUpdater) {
        super(editText, checkUpdater);
        setListenerForImageView(imageView);
    }

    @Override
    public void setListenerForImageView(final ImageView imageView) {
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable editable = mEditText.getText();
                int[] selectionLines = Util.getCurrentSelectionLines(mEditText);
                int start = Util.getThisLineStart(mEditText, selectionLines[0]);
                int end = Util.getThisLineEnd(mEditText, selectionLines[1]);

                // If all lines have bullet spans, remove all bullet spans. Otherwise, apply bullet
                // spans to all lines.
                if (getIsChecked()) {
                    ListBulletSpan[] listBulletSpans = editable.getSpans(start, end, ListBulletSpan.class);
                    for (ListBulletSpan listBulletSpan : listBulletSpans) {
                        editable.removeSpan(listBulletSpan);
                    }
                    updateCheckStatus(false);
                } else {
                    boolean hasListNumberSpan = false;
                    for (int line = selectionLines[0]; line <= selectionLines[1]; ++line) {
                        // Only add ListBulletSpan if there's no such span exists.
                        // For example, the current text is:
                        // * aa
                        // bb
                        //
                        // Then the user selects both lines and clicks bullet icon. In this case, the
                        // bullet span on "aa" should be kept, whereas, a bullet span should be
                        // added to "bb".
                        int lineStart = Util.getThisLineStart(mEditText, line);
                        int lineEnd = Util.getThisLineEnd(mEditText, line);
                        AreListSpan[] spans = editable.getSpans(lineStart, lineEnd, AreListSpan.class);
                        if (spans != null && spans.length > 0) {
                            AreListSpan span = spans[0];
                            if (span instanceof ListNumberSpan) {
                                hasListNumberSpan = true;
                                editable.removeSpan(span);
                                makeLineAsBullet(line, span.getDepth());
                            }
                        } else {
                            makeLineAsBullet(line, -1);
                        }
                    }
                    updateCheckStatus(true);
                    if (hasListNumberSpan) {
                        ARE_Style_ListNumber.reNumberBehindListItemSpansForLine(mEditText, selectionLines[1]);
                    }
                }

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
        ListBulletSpan[] listSpans = editable.getSpans(start, end,
                ListBulletSpan.class);
        if (null == listSpans || listSpans.length == 0) {
            return;
        }

        if (end > start) {
            //
            // User inputs
            //
            // To handle the \n case

            // int totalLen = editable.toString().length();
            // Util.log("ListNumber - total len == " + totalLen);
            char c = editable.charAt(end - 1);
            if (c == Constants.CHAR_NEW_LINE) {
                int currListSpanIndex = listSpans.length - 1;
                if (currListSpanIndex > -1) {
                    ListBulletSpan currListSpan = listSpans[currListSpanIndex];
                    int currListSpanStart = editable.getSpanStart(currListSpan);
                    int currListSpanEnd = editable.getSpanEnd(currListSpan);
                    CharSequence currItemSpanContent = editable.subSequence(currListSpanStart, currListSpanEnd);

                    if (isEmptyListItemSpan(currItemSpanContent)) {
                        //
                        // Handle this case:
                        // 1. A
                        // 2. <User types \n here, at an empty span>
                        //
                        // The 2 chars are:
                        // 1. ZERO_WIDTH_SPACE_STR
                        // 2. \n
                        //
                        // We need to remove current span and do not re-create
                        // span.
                        editable.removeSpan(currListSpan);

                        //
                        // Deletes the ZERO_WIDTH_SPACE_STR and \n
                        editable.delete(currListSpanStart, currListSpanEnd);
                        updateCheckStatus();
                    } else {
                        //
                        // Handle this case:
                        //
                        // 1. A
                        // 2. C
                        // 3. D
                        //
                        // User types \n after 'A'
                        // Then
                        // We should see:
                        // 1. A
                        // 2.
                        // 3. C
                        // 4. D
                        //
                        // We need to end the first span
                        // Then start the 2nd span
                        // Then reNumber the following list item spans
                        if (end > currListSpanStart) {
                            editable.removeSpan(currListSpan);
                            editable.setSpan(currListSpan,
                                    currListSpanStart, end - 1,
                                    Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        }
                        makeLineAsBullet(currListSpan.getDepth());
                    }
                } // #End of if it is in ListItemSpans..
            } // #End of user types \n
        } else {
            //
            // User deletes
            ListBulletSpan theFirstSpan = listSpans[0];
            if (listSpans.length > 0) {
                FindFirstAndLastBulletSpan findFirstAndLastBulletSpan = new FindFirstAndLastBulletSpan(editable, listSpans).invoke();
                theFirstSpan = findFirstAndLastBulletSpan.getFirstTargetSpan();
            }
            int spanStart = editable.getSpanStart(theFirstSpan);
            int spanEnd = editable.getSpanEnd(theFirstSpan);

            if (spanStart >= spanEnd) {
                // Case 1:
                // Since the last char of the span is deleted, we assume the user wants to remove
                // the span
                for (ListBulletSpan listSpan : listSpans) {
                    editable.removeSpan(listSpan);
                }

                //
                // To delete the previous span's \n
                // So the focus will go to the end of previous span
                if (spanStart > 0) {
                    editable.delete(spanStart - 1, spanEnd);
                }
            } else if (start != spanStart && start == spanEnd) {
                // Case 3
                // Since the user deletes the first char of the span, we assume he wants to remove
                // the span
                if (editable.length() > start) {
                    if (editable.charAt(start) == Constants.CHAR_NEW_LINE) {
                        ListBulletSpan[] spans = editable.getSpans(start, start, ListBulletSpan.class);
                        if (spans.length > 0) {
                            mergeForward(editable, theFirstSpan, spanStart, spanEnd);
                        }
                    } else {
                        mergeForward(editable, theFirstSpan, spanStart, spanEnd);
                    }
                }
            }
        }

        updateCheckStatus();
    } // # End of applyStyle(..)

    private void updateCheckStatus() {
        updateCheckStatus(ButtonCheckStatusUtil.shouldCheckButton(mEditText, ListBulletSpan.class));
    }

    protected void mergeForward(Editable editable, ListBulletSpan listSpan, int spanStart, int spanEnd) {
        // Util.log("merge forward 1");
        if (editable.length() <= spanEnd + 1) {
            return;
        }
        // Util.log("merge forward 2");
        ListBulletSpan[] targetSpans = editable.getSpans(
                spanEnd, spanEnd + 1, ListBulletSpan.class);
        if (targetSpans == null || targetSpans.length == 0) {
            return;
        }

        FindFirstAndLastBulletSpan findFirstAndLastBulletSpan = new FindFirstAndLastBulletSpan(editable, targetSpans).invoke();
        ListBulletSpan firstTargetSpan = findFirstAndLastBulletSpan.getFirstTargetSpan();
        ListBulletSpan lastTargetSpan = findFirstAndLastBulletSpan.getLastTargetSpan();
        int targetStart = editable.getSpanStart(firstTargetSpan);
        int targetEnd = editable.getSpanEnd(lastTargetSpan);
        // Util.log("merge to remove span start == " + targetStart + ", target end = " + targetEnd);

        int targetLength = targetEnd - targetStart;
        spanEnd = spanEnd + targetLength;

        for (ListBulletSpan targetSpan : targetSpans) {
            editable.removeSpan(targetSpan);
        }
        ListBulletSpan[] compositeSpans = editable.getSpans(spanStart, spanEnd, ListBulletSpan.class);
        for (ListBulletSpan lns : compositeSpans) {
            editable.removeSpan(lns);
        }
        editable.setSpan(listSpan, spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }

    private void makeLineAsBullet(int depth) {
        makeLineAsBullet(Util.getCurrentCursorLine(mEditText), depth);
    }

    /**
     * @param depth the depth of the new line. If depth is -1, the actual depth will be either
     *              the current depth if a ListSpan exists, or 1 if no ListSpan exists
     */
    private void makeLineAsBullet(int line, int depth) {
        Editable editable = mEditText.getText();
        int start = Util.getThisLineStart(mEditText, line);
        addZeroWidthSpaceStrSafe(editable, start);
        start = Util.getThisLineStart(mEditText, line);
        int end = Util.getThisLineEnd(mEditText, line);

        if (end < 1) {
            return;
        }
        if (editable.charAt(end - 1) == Constants.CHAR_NEW_LINE) {
            end--;
        }

        final ListBulletSpan listItemSpan;
        if (depth >= AreListSpan.MIN_DEPTH) {
            listItemSpan = new ListBulletSpan(depth, 0);
        } else {
            AreListSpan[] currSpans = editable.getSpans(start, end, AreListSpan.class);
            listItemSpan = new ListBulletSpan(
                    currSpans == null || currSpans.length == 0 ? 1 : currSpans[0].getDepth(), 0);
        }
        editable.setSpan(listItemSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    }

    private class FindFirstAndLastBulletSpan {
        private Editable editable;
        private ListBulletSpan[] targetSpans;
        private ListBulletSpan firstTargetSpan;
        private ListBulletSpan lastTargetSpan;

        public FindFirstAndLastBulletSpan(Editable editable, ListBulletSpan... targetSpans) {
            this.editable = editable;
            this.targetSpans = targetSpans;
        }

        public ListBulletSpan getFirstTargetSpan() {
            return firstTargetSpan;
        }

        public ListBulletSpan getLastTargetSpan() {
            return lastTargetSpan;
        }

        public FindFirstAndLastBulletSpan invoke() {
            firstTargetSpan = targetSpans[0];
            lastTargetSpan = targetSpans[0];
            if (targetSpans.length > 0) {
                int firstTargetSpanStart = editable.getSpanStart(firstTargetSpan);
                int lastTargetSpanEnd = editable.getSpanEnd(firstTargetSpan);
                for (ListBulletSpan lns : targetSpans) {
                    int lnsStart = editable.getSpanStart(lns);
                    int lnsEnd = editable.getSpanEnd(lns);
                    if (lnsStart < firstTargetSpanStart) {
                        firstTargetSpan = lns;
                        firstTargetSpanStart = lnsStart;
                    }
                    if (lnsEnd > lastTargetSpanEnd) {
                        lastTargetSpan = lns;
                        lastTargetSpanEnd = lnsEnd;
                    }
                }
            }
            return this;
        }
    }
}
