package com.chinalwb.are.styles.toolitems.styles;

import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.Constants;
import com.chinalwb.are.Util;
import com.chinalwb.are.spans.AreListSpan;
import com.chinalwb.are.spans.ListBulletSpan;
import com.chinalwb.are.spans.ListNumberSpan;
import com.chinalwb.are.styles.ARE_ABS_FreeStyle;
import com.chinalwb.are.styles.ButtonCheckStatusUtil;
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

    /**
     * There are four cases for the selection (can be multiple lines):
     *   Case 1: the selection doesn't have any bullet or number span.
     *           1. aa
     *           bb  (selection start)
     *           cc  (selection end)
     *           1. dd
     *           We need to add the ListBulletSpan to the selection
     *   Case 2: the selection includes only ListBulletSpan.
     *           1. aa
     *           *. bb (selection start)
     *           *. cc (selection end)
     *           *. dd
     *           We need to remove the ListBulletSpan.
     *   Case 3: the selection includes only ListNumberSpan.
     *           1. aa
     *           2. bb (selection start)
     *           3. cc (selection end)
     *           4. dd
     *           We need to convert the ListNumberSpan to a ListBulletSpan with the same depth.
     *    Case 4: the selection includes a mix of ListNumberSpan, ListBulletSpan, and no span
     *           1. aa
     *           2. bb (selection start)
     *           *. cc
     *           dd    (selection end)
     *           1. ee
     *           See above 3 cases
     *           Note we don't need to (and shouldn't) add any ListBulletSpan to lines
     *           that already have it.
     */
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
                Util.triggerEditableRedraw(mEditText, editable, selectionLines);
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
            // The current editing is either:
            //   1) A normal insertion or deletion of chars that doesn't require any special check
            //   2) Or, a NEW_LINE insertion, which requires special checks.
            char c = editable.charAt(end - 1);
            // Only do special checks if the user inputs \n (new line).
            // No need for special checks if the user inputs normal characters.
            if (c == Constants.CHAR_NEW_LINE) {
                int currListSpanIndex = listSpans.length - 1;
                if (currListSpanIndex > -1) {
                    ListBulletSpan currListSpan = listSpans[currListSpanIndex];
                    int currListSpanStart = editable.getSpanStart(currListSpan);
                    int currListSpanEnd = editable.getSpanEnd(currListSpan);
                    CharSequence currItemSpanContent = editable.subSequence(currListSpanStart, currListSpanEnd);
                    // If the last list span is empty
                    // For example:
                    //   1. AA
                    //       1. aa
                    //       *. <User types \n here, which is an empty span>
                    //   2. BB
                    // Or:
                    //   1. AA
                    //       1. ZERO_WIDTH_SPACE_STR
                    //       *. <User types \n here>
                    //   2. BB
                    //
                    // We need to remove the current span (to make this line an empty line), and
                    // renumber lines after it.
                    // Note that we shouldn't add any new span to it.
                    //
                    // If the last list span is not empty
                    // For example:
                    //   *. aa <User types \n here, which is not an empty span>
                    //   *. bb
                    //   *. cc
                    // Or:
                    //   *. aa <User types \n here> aaaa
                    //   *. bb
                    //
                    // We need to: 1) end the span right before the cursor, 2) start a new span at
                    // the cursor, 3) update following list items
                    if (isEmptyListItemSpan(currItemSpanContent)) {
                        editable.removeSpan(currListSpan);
                        editable.delete(currListSpanStart, currListSpanEnd);
                        ARE_Style_ListNumber.reNumberBehindListItemSpansForOffset(mEditText, currListSpanStart);
                    } else {
                        if (end > currListSpanStart) {
                            editable.removeSpan(currListSpan);
                            // The end of new span is "end - 1" not "end" because the two spans need
                            // to be separated (by a invisible char). Otherwise, the second span
                            // will be added to the first span, resulting only one span with
                            // incorrect order.
                            editable.setSpan(currListSpan,
                                    currListSpanStart, end - 1,
                                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        }
                        makeLineAsBullet(currListSpan.getDepth());
                        // No need to reNumber because the current line and previous line are both
                        // ListBullet, which won't affect any order.
                    }
                } // #End of if it is in ListItemSpans..
            } // #End of user types \n
        } else {
            // The in-editing word is empty (after editing)
            AreListSpan theFirstSpan = listSpans[0];
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
            ARE_Style_ListNumber.reNumberBehindListItemSpansForOffset(mEditText, spanEnd);
        }

        updateCheckStatus();
    } // # End of applyStyle(..)

    private void updateCheckStatus() {
        updateCheckStatus(ButtonCheckStatusUtil.shouldCheckButton(mEditText, ListBulletSpan.class));
    }

    protected void mergeForward(Editable editable, AreListSpan listSpan, int spanStart, int spanEnd) {
        // Util.log("merge forward 1");
        if (editable.length() <= spanEnd + 1) {
            return;
        }
        // Util.log("merge forward 2");
        AreListSpan[] targetSpans = editable.getSpans(spanEnd, spanEnd + 1, AreListSpan.class);
        if (targetSpans == null || targetSpans.length == 0) {
            return;
        }

        FindFirstAndLastBulletSpan findFirstAndLastBulletSpan = new FindFirstAndLastBulletSpan(editable, targetSpans).invoke();
        AreListSpan firstTargetSpan = findFirstAndLastBulletSpan.getFirstTargetSpan();
        AreListSpan lastTargetSpan = findFirstAndLastBulletSpan.getLastTargetSpan();
        int targetStart = editable.getSpanStart(firstTargetSpan);
        int targetEnd = editable.getSpanEnd(lastTargetSpan);
        // Util.log("merge to remove span start == " + targetStart + ", target end = " + targetEnd);

        int targetLength = targetEnd - targetStart;
        spanEnd = spanEnd + targetLength;

        for (AreListSpan targetSpan : targetSpans) {
            editable.removeSpan(targetSpan);
        }
        AreListSpan[] compositeSpans = editable.getSpans(spanStart, spanEnd, AreListSpan.class);
        for (AreListSpan lns : compositeSpans) {
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
        editable.setSpan(listItemSpan, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
    }

    private class FindFirstAndLastBulletSpan {
        private Editable editable;
        private AreListSpan[] targetSpans;
        private AreListSpan firstTargetSpan;
        private AreListSpan lastTargetSpan;

        public FindFirstAndLastBulletSpan(Editable editable, AreListSpan... targetSpans) {
            this.editable = editable;
            this.targetSpans = targetSpans;
        }

        public AreListSpan getFirstTargetSpan() {
            return firstTargetSpan;
        }

        public AreListSpan getLastTargetSpan() {
            return lastTargetSpan;
        }

        public FindFirstAndLastBulletSpan invoke() {
            firstTargetSpan = targetSpans[0];
            lastTargetSpan = targetSpans[0];
            if (targetSpans.length > 0) {
                int firstTargetSpanStart = editable.getSpanStart(firstTargetSpan);
                int lastTargetSpanEnd = editable.getSpanEnd(firstTargetSpan);
                for (AreListSpan lns : targetSpans) {
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
