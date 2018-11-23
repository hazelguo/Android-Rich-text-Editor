package com.chinalwb.are.styles.toolitems.styles;

import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.ButtonCheckStatusUtil;
import com.chinalwb.are.Constants;
import com.chinalwb.are.Util;
import com.chinalwb.are.spans.ListBulletSpan;
import com.chinalwb.are.spans.ListNumberSpan;
import com.chinalwb.are.styles.ARE_ABS_FreeStyle;
import com.chinalwb.are.styles.toolitems.IARE_ToolItem_Updater;

/**
 * All Rights Reserved.
 *
 * @author Wenbin Liu
 */
public class ARE_Style_ListNumber extends ARE_ABS_FreeStyle {

    private AREditText mEditText;

    private ImageView mListNumberImageView;

    private IARE_ToolItem_Updater mCheckUpdater;

    private boolean mListBulletChecked;

    public ARE_Style_ListNumber(AREditText editText, ImageView imageView, IARE_ToolItem_Updater checkUpdater) {
        super(editText.getContext());
        this.mEditText = editText;
        this.mListNumberImageView = imageView;
        mCheckUpdater = checkUpdater;
        setListenerForImageView(this.mListNumberImageView);
    }

    @Override
    public EditText getEditText() {
        return this.mEditText;
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
                        followingStartNumber = previousListItemSpan.getNumber();
                    }
                    for (int line = selectionLines[0]; line <= selectionLines[1]; ++line) {
                        followingStartNumber ++;
                        int lineStart = Util.getThisLineStart(editText, line);
                        int lineEnd = Util.getThisLineEnd(editText, line);
                        int nextSpanStart = editable.nextSpanTransition(lineStart - 1, lineEnd, ListNumberSpan.class);
                        if (nextSpanStart >= lineEnd) {
                            makeLineAsList(line, followingStartNumber);
                        }
                    }
                    setChecked(true);
                }
                // Reget the end of selection because the text length may change as we add/remove spans
                reNumberBehindListItemSpans(
                        Util.getThisLineEnd(editText, selectionLines[1]) + 1,
                        editable, followingStartNumber);
            }
        });
    }

    /**
     * @param start the start of the in-editing word
     *              Look at TextWatcher.afterTextChange.start
     * @param end the end of the in-editing word
     *            Look at TextWatcher.afterTextChange.end
     */
    @Override
    public void applyStyle(Editable editable, int start, int end) {
        ListNumberSpan[] listSpans = editable.getSpans(start, end, ListNumberSpan.class);
        if (null == listSpans || listSpans.length == 0) {
            return;
        }

        // Check if the in-editing word is empty (after editing)
        if (end > start) {
            // If it's not empty (after editing), the current editing is either:
            //   1) A normal insertion or deletion of chars that doesn't require any special check
            //   2) Or, a NEW_LINE insertion, which requires special checks.
            char c = editable.charAt(end - 1);
            // Only do special checks if the user inputs \n (new line).
            // No need for special checks if the user inputs normal characters.
            if (c == Constants.CHAR_NEW_LINE) {
                int lastListSpanIndex = listSpans.length - 1;
                if (lastListSpanIndex > -1) {
                    ListNumberSpan lastListSpan = listSpans[lastListSpanIndex];
                    int lastListItemSpanStart = editable.getSpanStart(lastListSpan);
                    int lastListItemSpanEnd = editable.getSpanEnd(lastListSpan);
                    CharSequence listItemSpanContent = editable.subSequence(
                            lastListItemSpanStart, lastListItemSpanEnd);

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
                    if (isEmptyListItemSpan(listItemSpanContent)) {
                        editable.removeSpan(lastListSpan);
                        // Deletes the ZERO_WIDTH_SPACE_STR and \n
                        editable.delete(lastListItemSpanStart, lastListItemSpanEnd);
                        // Restart the number for any list spans after the removed span.
                        reNumberBehindListItemSpans(lastListItemSpanStart, editable, 0);
                    } else {
                        if (end > lastListItemSpanStart) {
                            editable.removeSpan(lastListSpan);
                            editable.setSpan(lastListSpan,
                                    lastListItemSpanStart, end - 1,
                                    Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        }
                        int lastListItemNumber = lastListSpan.getNumber();
                        ListNumberSpan newListItemSpan = makeLineAsList(lastListItemNumber + 1);
                        end = editable.getSpanEnd(newListItemSpan);
                        reNumberBehindListItemSpans(end, editable, newListItemSpan.getNumber());
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
                int firstSpanNumber = firstSpan.getNumber();
                for (ListNumberSpan lns : listSpans) {
                    if (lns.getNumber() < firstSpanNumber) {
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
                        int removedNumber = firstSpan.getNumber();
                        reNumberBehindListItemSpans(spanStart, editable, removedNumber - 1);
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
                int previousNumber = firstSpan.getNumber();
                reNumberBehindListItemSpans(end, editable, previousNumber);
            }
        }
        updateCheckStatus();
    } // # End of applyStyle(..)

    protected void mergeForward(Editable editable, ListNumberSpan listSpan, int spanStart, int spanEnd) {
        Util.log("merge forward 1");
        if (editable.length() <= spanEnd + 1) {
            return;
        }
        Util.log("merge forward 2");
        ListNumberSpan[] targetSpans = editable.getSpans(spanEnd, spanEnd + 1, ListNumberSpan.class);
        // logAllListItems(editable, false);
        if (targetSpans == null || targetSpans.length == 0) {
            reNumberBehindListItemSpans(spanEnd, editable, listSpan.getNumber());
            return;
        }
        ListNumberSpan firstTargetSpan = targetSpans[0];
        ListNumberSpan lastTargetSpan = targetSpans[0];

        if (targetSpans.length > 0) {
            int firstTargetSpanNumber = firstTargetSpan.getNumber();
            int lastTargetSpanNumber = lastTargetSpan.getNumber();
            for (ListNumberSpan lns : targetSpans) {
                int lnsNumber = lns.getNumber();
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
        Util.log("merge to remove span start == " + targetStart + ", target end = " + targetEnd + ", target number = " + firstTargetSpan.getNumber());

        int targetLength = targetEnd - targetStart;
        spanEnd = spanEnd + targetLength;
        for (ListNumberSpan targetSpan : targetSpans) {
            editable.removeSpan(targetSpan);
        }
        ListNumberSpan[] compositeSpans = editable.getSpans(spanStart, spanEnd, ListNumberSpan.class);
        for (ListNumberSpan lns : compositeSpans) {
            editable.removeSpan(lns);
        }
        editable.setSpan(listSpan, spanStart, spanEnd,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        Util.log("merge span start == " + spanStart + " end == " + spanEnd);
        reNumberBehindListItemSpans(spanEnd, editable, listSpan.getNumber());
    }

    private void logAllListItems(Editable editable, boolean printDetail) {
        ListNumberSpan[] listItemSpans = editable.getSpans(0,
                editable.length(), ListNumberSpan.class);
        for (ListNumberSpan span : listItemSpans) {
            int ss = editable.getSpanStart(span);
            int se = editable.getSpanEnd(span);
            int flag = editable.getSpanFlags(span);
            Util.log("List All: " + span.getNumber() + " :: start == " + ss
                    + ", end == " + se + ", flag == " + flag);
           if (printDetail) {
               for (int i = ss; i < se; i++) {
                   Util.log("char at " + i + " = " + editable.charAt(i) + " int = " + ((int) (editable.charAt(i))));
               }

               if (editable.length() > se) {
                   Util.log("char at " + se + " = " + editable.charAt(se)+ " int = " + ((int) (editable.charAt(se))));
               }
           }
        }
    }

    /**
     * Check if this is an empty span.
     * For example:
     *   1. a
     *   2.
     *
     * Line 2 is empty
     */
    private boolean isEmptyListItemSpan(CharSequence listItemSpanContent) {
        int spanLen = listItemSpanContent.length();
        return spanLen == 2;
    }

    private ListNumberSpan makeLineAsList(int num) {
        EditText editText = getEditText();
        return makeLineAsList(Util.getCurrentCursorLine(editText), num);
    }

    private ListNumberSpan makeLineAsList(int line, int num) {
        Util.log("make line as list: " + line + " , " + num);
        EditText editText = getEditText();
        int start = Util.getThisLineStart(editText, line);
        Editable editable = editText.getText();
        editable.insert(start, Constants.ZERO_WIDTH_SPACE_STR);
        start = Util.getThisLineStart(editText, line);
        int end = Util.getThisLineEnd(editText, line);

        if (end > 0 && editable.charAt(end - 1) == Constants.CHAR_NEW_LINE) {
            end--;
        }

        ListNumberSpan listItemSpan = new ListNumberSpan(num);
        editable.setSpan(listItemSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        return listItemSpan;
    }

    /**
     * @param end
     * @param editable
     * @param thisNumber
     */
    public static void reNumberBehindListItemSpans(int end, Editable editable,
                                                   int thisNumber) {
        ListNumberSpan[] behindListItemSpans = editable.getSpans(end + 1,
                end + 2, ListNumberSpan.class);
        if (null != behindListItemSpans && behindListItemSpans.length > 0) {
            int total = behindListItemSpans.length;
            int index = 0;
            for (ListNumberSpan listItemSpan : behindListItemSpans) {
                int newNumber = ++thisNumber;
                Util.log("Change old number == " + listItemSpan.getNumber()
                        + " to new number == " + newNumber);
                listItemSpan.setNumber(newNumber);
                ++index;
                if (total == index) {
                    int newSpanEnd = editable.getSpanEnd(listItemSpan);
                    reNumberBehindListItemSpans(newSpanEnd, editable, newNumber);
                }
            }
        }
    }

    private void updateCheckStatus() {
        updateCheckStatus(ButtonCheckStatusUtil.shouldCheckButton(getEditText(), ListNumberSpan.class));
    }

    private void updateCheckStatus(boolean isChecked) {
        setChecked(isChecked);
        if (mCheckUpdater != null) {
            mCheckUpdater.onCheckStatusUpdate(isChecked);
        }
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
        return mListBulletChecked;
    }
}