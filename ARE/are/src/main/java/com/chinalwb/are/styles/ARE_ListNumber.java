package com.chinalwb.are.styles;

import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;

import com.chinalwb.are.ButtonCheckStatusUtil;
import com.chinalwb.are.Constants;
import com.chinalwb.are.Util;
import com.chinalwb.are.spans.AreListSpan;
import com.chinalwb.are.spans.ListBulletSpan;
import com.chinalwb.are.spans.ListNumberSpan;

import java.util.ArrayList;
import java.util.List;

import static com.chinalwb.are.Util.addZeroWidthSpaceStrSafe;
import static com.chinalwb.are.Util.isEmptyListItemSpan;

/**
 * All Rights Reserved.
 *
 * @author Wenbin Liu
 */
@Deprecated
public class ARE_ListNumber extends ARE_ABS_FreeStyle {

    private ImageView mListNumberImageView;

    private boolean toMergeForward = false;

    private boolean mListBulletChecked;

    public ARE_ListNumber(ImageView imageView) {
        this.mListNumberImageView = imageView;
        setListenerForImageView(this.mListNumberImageView);
    }

    /**
     * There are four cases for the selection (can be multiple lines):
     *   Case 1: the selection doesn't have any bullet or number span.
     *           1. aa
     *           bb  (selection start)
     *           cc  (selection end)
     *           1. dd
     *           We need to add the ListNumberSpan to the selection, and update any ListNumberSpan
     *           right before or after it.
     *   Case 2: the selection includes only ListNumberSpan.
     *           1. aa
     *           2. bb (selection start)
     *           3. cc (selection end)
     *           4. dd
     *           We need to remove the ListNumberSpan, and update any ListNumberSpan right after it.
     *   Case 3: the selection includes only ListBulletSpan.
     *           1. aa
     *           *. bb (selection start)
     *           *. cc (selection end)
     *           1. dd
     *           We need to remove all the ListBulletSpan, and handle this case in the same way as
     *           Case 1.
     *    Case 4: the selection includes a mix of ListNumberSpan, ListBulletSpan, and no span
     *           1. aa
     *           2. bb (selection start)
     *           *. cc
     *           dd    (selection end)
     *           1. ee
     *           We need to remove all the ListBulletSpan, and handle this case in the way similar
     *           to Case 1. Note we don't need to (and shouldn't) add any ListNumberSpan to lines
     *           that already have it.
     */
    @Override
    public void setListenerForImageView(final ImageView imageView) {
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = getEditText();
                Editable editable = editText.getText();
                int[] selectionLines = Util.getCurrentSelectionLines(editText);
                // Note that we use start & end instead of selectionStart & selectionEnd because
                // partial selection should be treated as full-line selection in number span.
                int start = Util.getThisLineStart(editText, selectionLines[0]);
                int end = Util.getThisLineEnd(editText, selectionLines[1]);

                // Remove all ListBulletSpan in the selection:
                //   Convert Case 3 to Case 1.
                //   Convert Case 4 to a similar case as Case 1 as I explained above.
                // Note that we don't need to reorder any following ListNumberSpan yet as we will
                // do it as part of the Case 1
                ListBulletSpan[] listBulletSpans = editable.getSpans(start, end, ListBulletSpan.class);
                if (listBulletSpans != null && listBulletSpans.length > 0) {
                    for (ListBulletSpan listBulletSpan : listBulletSpans) {
                        editable.removeSpan(listBulletSpan);
                    }
                }

                // If all lines in the selection have number spans, remove all number span. (Case 2)
                // Otherwise, apply number span to lines that don't have number span. (Case 1, 3, 4)
                // Always reorder following number spans afterwards.
                int followingStartNumber = 0;
                if (getIsChecked()) {
                    ListNumberSpan[] listNumberSpans = editable.getSpans(start, end, ListNumberSpan.class);
                    for (ListNumberSpan listNumberSpan : listNumberSpans) {
                        editable.removeSpan(listNumberSpan);
                    }
                    setChecked(false);
                } else {
                    ListNumberSpan[] aheadListNumberSpans = editable.getSpans(
                            start - 2, start - 1, ListNumberSpan.class);
                    if (null != aheadListNumberSpans && aheadListNumberSpans.length > 0) {
                        ListNumberSpan previousListItemSpan = aheadListNumberSpans[aheadListNumberSpans.length - 1];
                        followingStartNumber = previousListItemSpan.getOrder();
                    }
                    for (int line = selectionLines[0]; line <= selectionLines[1]; ++line) {
                        followingStartNumber ++;

                        int lineStart = Util.getThisLineStart(editText, line);
                        int lineEnd = Util.getThisLineEnd(editText, line);
                        ListNumberSpan[] spans = editable.getSpans(lineStart, lineEnd, ListNumberSpan.class);
                        if (spans != null && spans.length > 0) {
                            spans[0].setOrder(followingStartNumber);
                        } else {
                            makeLineAsList(line, followingStartNumber);
                        }
                    }
                    setChecked(true);
                }
                // Reget the end of selection because the text length may change as we add/remove spans
                reNumberBehindListItemSpansForLine(editText, selectionLines[1]);

                for (int line = selectionLines[0]; line <= selectionLines[1]; ++line) {
                    int lineStart = Util.getThisLineStart(editText, line);
                    // -- Change the content to trigger the editable redraw
                    editable.insert(lineStart, Constants.ZERO_WIDTH_SPACE_STR);
                    editable.delete(lineStart + 1, lineStart + 1);
                    // -- End: Change the content to trigger the editable redraw
                }
            }
        });
    }

    /**
     * @param start the start of the in-editing word if the word is not empty (after editing), or
     *              the cursor position if the word is empty (after editing).
     *              Look at TextWatcher.afterTextChange.start
     * @param end the end of the change (after editing). It's either the cursor position, or the
     *            end of the line if it's a selection change.
     *            Look at TextWatcher.afterTextChange.end
     */
    @Override
    public void applyStyle(Editable editable, int start, int end) {
        ListNumberSpan[] listSpans = editable.getSpans(start, end, ListNumberSpan.class);
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
                    ListNumberSpan currListSpan = listSpans[currListSpanIndex];
                    int currListSpanStart = editable.getSpanStart(currListSpan);
                    int currListSpanEnd = editable.getSpanEnd(currListSpan);
                    CharSequence currSpanContent = editable.subSequence(currListSpanStart, currListSpanEnd);

                    // If the last list span is empty
                    // For example:
                    //   1. aa
                    //   2. <User types \n here, which is an empty span>
                    // Or:
                    //   1. ZERO_WIDTH_SPACE_STR
                    //   2. \n
                    //
                    // We need to remove the current span (to make this line an empty line)
                    // Note that we shouldn't add any new span to it.
                    //
                    // If the last list span is not empty
                    // For example:
                    //   1. aa <User types \n here, which is not an empty span>
                    //   2. bb
                    //   3. cc
                    // Or:
                    //   1. aa <User types \n here> aaaa
                    //   2. bb
                    //
                    // We need to: 1) end the span right before the cursor, 2) start a new span at
                    // the cursor, 3) update following ListNumber items
                    if (isEmptyListItemSpan(currSpanContent)) {
                        editable.removeSpan(currListSpan);
                        // Deletes the ZERO_WIDTH_SPACE_STR and \n
                        editable.delete(currListSpanStart, currListSpanEnd);
                        // Restart the number for any list spans after the removed span.
                        reNumberBehindListItemSpansForOffset(getEditText(), currListSpanStart);
                    } else {
                        if (end > currListSpanStart) {
                            editable.removeSpan(currListSpan);
                            editable.setSpan(currListSpan,
                                    currListSpanStart, end - 1,
                                    Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        }
                        makeLineAsList(currListSpan);
                        reNumberBehindListItemSpansForOffset(getEditText(), end);
                    }
                }
            }
            updateCheckStatus();
        } else {
            // The in-editing word is empty (after editing)
            int spanStart = editable.getSpanStart(listSpans[0]);
            int spanEnd = editable.getSpanEnd(listSpans[0]);
            ListNumberSpan firstSpan = listSpans[0];
            if (listSpans.length > 1) {
                int firstSpanNumber = firstSpan.getOrder();
                for (ListNumberSpan lns : listSpans) {
                    if (lns.getOrder() < firstSpanNumber) {
                        firstSpan = lns;
                    }
                }
                spanStart = editable.getSpanStart(firstSpan);
                spanEnd = editable.getSpanEnd(firstSpan);
            }

            if (spanStart >= spanEnd) {
                // We assume the user wants to remove the span as the span is empty
                for (ListNumberSpan listSpan : listSpans) {
                    editable.removeSpan(listSpan);
                }
                // Delete the previous span's \n so that the focus will go to the end of previous span
                if (spanStart > 0) {
                    editable.delete(spanStart - 1, spanEnd);
                }

                if (editable.length() > spanEnd) {
                    ListNumberSpan[] spansBehind = editable.getSpans(spanEnd, spanEnd + 1, ListNumberSpan.class);
                    if (spansBehind.length > 0) {
                        reNumberBehindListItemSpansForOffset(getEditText(), spanStart);
                    }
                }
            } else if (start == spanStart) {
                // The user just removes the last char in the span. We have a invisible placeholder
                // to keep the number span showing.
                return;
            } else if (start == spanEnd) {
                // We assume the user wants to remove the span as the first char of the span is deleted
                // The removed char is not the end of the EditText
                if (editable.length() > start) {
                    if (editable.charAt(start) == Constants.CHAR_NEW_LINE) {
                        ListNumberSpan[] spans = editable.getSpans(start, start, ListNumberSpan.class);
                        if (spans.length > 0) {
                            mergeForward(editable, firstSpan, spanStart, spanEnd);
                        } else {
                            editable.removeSpan(spans[0]);
                        }
                    } else {
                        mergeForward(editable, firstSpan, spanStart, spanEnd);
                    }
                }
            } else {
                //
                // Handle this case:
                // 1. A
                // 2. B
                // x
                // 1. C
                // 2. D
                //
                // When user deletes the "x"
                // Then merge two lists, so it should be changed to:
                // 1. A
                // 2. B
                // 3. C
                // 4. D
                //
                // mergeLists();
                reNumberBehindListItemSpansForOffset(getEditText(), end);
            }
        }
        updateCheckStatus();
    } // # End of applyStyle(..)

    protected void mergeForward(Editable editable, ListNumberSpan listSpan, int spanStart, int spanEnd) {
        if (editable.length() <= spanEnd + 1) {
            return;
        }
        ListNumberSpan[] targetSpans = editable.getSpans(spanEnd, spanEnd + 1, ListNumberSpan.class);
        // logAllListItems(editable, false);
        if (targetSpans == null || targetSpans.length == 0) {
            reNumberBehindListItemSpansForOffset(getEditText(), spanEnd);
            return;
        }
        ListNumberSpan firstTargetSpan = targetSpans[0];
        ListNumberSpan lastTargetSpan = targetSpans[0];

        if (targetSpans.length > 0) {
            int firstTargetSpanNumber = firstTargetSpan.getOrder();
            int lastTargetSpanNumber = lastTargetSpan.getOrder();
            for (ListNumberSpan lns : targetSpans) {
                int lnsNumber = lns.getOrder();
                if (lnsNumber < firstTargetSpanNumber) {
                    firstTargetSpan = lns;
                    firstTargetSpanNumber = lnsNumber;
                }
                if (lnsNumber > lastTargetSpanNumber) {
                    lastTargetSpan = lns;
                    lastTargetSpanNumber = lnsNumber;
                }
            }
        }
        int targetStart = editable.getSpanStart(firstTargetSpan);
        int targetEnd = editable.getSpanEnd(lastTargetSpan);

        int targetLength = targetEnd - targetStart;
        spanEnd = spanEnd + targetLength;
        for (ListNumberSpan targetSpan : targetSpans) {
            editable.removeSpan(targetSpan);
        }
        ListNumberSpan[] compositeSpans = editable.getSpans(spanStart, spanEnd, ListNumberSpan.class);
        for (ListNumberSpan lns : compositeSpans) {
            editable.removeSpan(lns);
        }
        editable.setSpan(listSpan, spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        reNumberBehindListItemSpansForOffset(getEditText(), spanEnd);
    }

    private ListNumberSpan makeLineAsList(ListNumberSpan prevSpan) {
        EditText editText = getEditText();
        return makeLineAsList(Util.getCurrentCursorLine(editText), prevSpan.getOrder() + 1);
    }

    private ListNumberSpan makeLineAsList(int line, int num) {
        EditText editText = getEditText();
        Editable editable = editText.getText();
        int start = Util.getThisLineStart(editText, line);
        addZeroWidthSpaceStrSafe(editable, start);
        start = Util.getThisLineStart(editText, line);
        int end = Util.getThisLineEnd(editText, line);

        if (end > 0 && editable.charAt(end - 1) == Constants.CHAR_NEW_LINE) {
            end--;
        }

        AreListSpan[] currSpans = editable.getSpans(start, end, AreListSpan.class);
        ListNumberSpan listItemSpan = new ListNumberSpan(
                currSpans == null || currSpans.length == 0 ? 1 : currSpans[0].getDepth(), num);
        editable.setSpan(listItemSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        return listItemSpan;
    }


    public static void reNumberInsideListItemSpans(EditText editText, int startLine, int endLine) {
        List<Integer> depthToOrder = getDepthToOrderList(editText, startLine - 1);
        Editable editable = editText.getText();

        for (int line = startLine; line <= endLine; ++line) {
            AreListSpan[] listSpans = Util.getListSpanForLine(editText, editable, line);
            if (listSpans[0] instanceof ListNumberSpan) {
                ListNumberSpan span = (ListNumberSpan) listSpans[0];
                span.setOrder(depthToOrder.get(span.getDepth()));
            }
            updateDepthToOrderFromSpan(listSpans[0], depthToOrder);
        }
    }

    /**
     * DepthToOrder[d]: for lines after `line`, if the depth is d, the order is DepthToOrder[d].
     *
     * How to get DepthToOrder:
     *   Iterate from the startLine to the very first line to find the last no list span line.
     *
     *   Iterate from the last no list span line to the startLine. Say the current line's list span
     *   has depth d' and order o'. If:
     *     1. the current line is a list bullet span:
     *       a. any following list number span that has a larger depth should have order 1
     *       b. any following list number span that has the same depth should have order 1
     *       c. any following list number span that has a smaller depth should not be affected.
     *     2. the current line is a list bullet span:
     *       a. any following list number span that has a larger depth should have order 1
     *       b. any following list number span that has the same depth should have order o' + 1
     *       c. any following list number span that has a smaller depth should not be affected.
     *
     *   Update DepthToOrder as processing lines.
     *
     *   The actual implementation is in a reversed way: iterate the depth, and look for the last
     *   line that can affect the order of this depth.
     *
     *   @param line the end (inclusive) of the calculation
     */
    private static List<Integer> getDepthToOrderList(EditText editText, int line) {
        Editable editable = editText.getText();
        List<Integer> depthToOrder = new ArrayList<>(AreListSpan.MAX_DEPTH + 1);
        for (int i = 0; i <= AreListSpan.MAX_DEPTH; ++i) {
            depthToOrder.add(1);
        }

        int lastNoListSpanLine = line;
        for (; lastNoListSpanLine > -1; lastNoListSpanLine--) {
            int lineStart = Util.getThisLineStart(editText, lastNoListSpanLine);
            int lineEnd = Util.getThisLineEnd(editText, lastNoListSpanLine);
            int nextSpanStart = editable.nextSpanTransition(lineStart - 1, lineEnd, AreListSpan.class);
            if (nextSpanStart >= lineEnd) {
                break;
            }
        }

        for (int l = lastNoListSpanLine + 1; l <= line; ++l) {
            AreListSpan[] listSpans = Util.getListSpanForLine(editText, editable, l);
            updateDepthToOrderFromSpan(listSpans[0], depthToOrder);
        }

        return depthToOrder;
    }

    private static void updateDepthToOrderFromSpan(AreListSpan span, List<Integer> depthToOrder) {
        int startDepth = span.getDepth();
        if (span instanceof ListNumberSpan) {
            depthToOrder.set(startDepth, span.getOrder() + 1);
            startDepth++;
        }
        for (int i = startDepth; i <= AreListSpan.MAX_DEPTH; ++i) {
            depthToOrder.set(i, 1);
        }
    }

    /**
     * @param line the line number right before the pending update block. Thus, `line` doesn't need
     *             to be updated.
     */
    public static void reNumberBehindListItemSpansForLine(EditText editText, int line) {
        Editable editable = editText.getText();
        List<Integer> depthToOrder = getDepthToOrderList(editText, line);
        for (int l = line; ; l++) {
            int lineStart = Util.getThisLineStart(editText, l);
            AreListSpan[] spans = editable.getSpans(lineStart, lineStart + 1, AreListSpan.class);
            if (spans == null || spans.length == 0) {
                break;
            }

            if (spans[0] instanceof ListNumberSpan) {
                ListNumberSpan span = (ListNumberSpan) spans[0];
                span.setOrder(depthToOrder.get(span.getDepth()));
            }
            updateDepthToOrderFromSpan(spans[0], depthToOrder);
        }
    }

    /**
     * @param offset the offset right before the pending update block. Thus, `offset` shouldn't be
     *               updated
     */
    public static void reNumberBehindListItemSpansForOffset(EditText editText, int offset) {
        reNumberBehindListItemSpansForLine(editText, Util.getLineForOffset(editText, offset + 1));
    }

    private void updateCheckStatus() {
        setChecked(ButtonCheckStatusUtil.shouldCheckButton(getEditText(), ListNumberSpan.class));
    }

    @Override
    public ImageView getImageView() {
        return this.mListNumberImageView;
    }

    @Override
    public void setChecked(boolean isChecked) {
        mListBulletChecked = isChecked;
    }

    @Override
    public boolean getIsChecked() {
        return this.mListBulletChecked;
    }
}