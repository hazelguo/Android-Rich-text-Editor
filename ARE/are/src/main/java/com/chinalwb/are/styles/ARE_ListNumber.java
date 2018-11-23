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
import com.chinalwb.are.spans.ListBulletSpan;
import com.chinalwb.are.spans.ListNumberSpan;

/**
 * All Rights Reserved.
 *
 * @author Wenbin Liu
 */
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
                        followingStartNumber = previousListItemSpan.getNumber();
                    }
                    for (int line = selectionLines[0]; line <= selectionLines[1]; ++line) {
                        int lineStart = Util.getThisLineStart(editText, line);
                        int lineEnd = Util.getThisLineEnd(editText, line);
                        int nextSpanStart = editable.nextSpanTransition(lineStart - 1, lineEnd, ListNumberSpan.class);
                        if (nextSpanStart >= lineEnd) {
                            makeLineAsList(line, followingStartNumber);
                        }
                    }
                    setChecked(true);
                }
                reNumberBehindListItemSpans(end + 1, editable, followingStartNumber);

                ////////////////////////////////////////////////////////////////////////////////
                //
                // Check if there is any ListNumberSpan first.
                // If there is ListNumberSpan, it means this case:
                // User has typed in:
                //
                // * aa
                // * bb
                // * cc
                //
                // Then user clicks the Number icon at 1 or 2 or any other item
                // He wants to change current ListBulletSpan to ListNumberSpan
                //
                // So it becomes:
                // For example: user clicks Number icon at 2:
                // * aa
                // 1. bb
                // * cc

//                int selectionStart = editText.getSelectionStart();
//                int selectionEnd = editText.getSelectionEnd();
//                ListBulletSpan[] listBulletSpans = editable.getSpans(selectionStart,
//                        selectionEnd, ListBulletSpan.class);
//                if (null != listBulletSpans && listBulletSpans.length > 0) {
//                    changeListBulletSpanToListNumberSpan(editable, listBulletSpans);
//                    return;
//                }
//
//                ListNumberSpan[] listNumberSpans = editable.getSpans(start, end,
//                        ListNumberSpan.class);
//                if (null == listNumberSpans || listNumberSpans.length == 0) {
//                    //
//                    // Current line is not list item span
//                    // By clicking the image view, we should make it as
//                    // ListItemSpan
//                    // And ReOrder
//                    //
//                    // ------------ CASE 1 ------------------
//                    // Case 1:
//                    // Nothing types in, user just clicks the List image
//                    // For this case we need to mark it as ListItemSpan
//
//                    //
//                    // ------------ CASE 2 ------------------
//                    // Case 2:
//                    // Before or after the current line, there are already
//                    // ListItemSpan have been made
//                    // Like:
//                    // 1. AAA
//                    // BBB
//                    // 1. CCC
//                    //
//                    // User puts cursor to the 2nd line: BBB
//                    // And clicks the List image
//                    // For this case we need to make current line as
//                    // ListItemSpan
//                    // And, we should also reOrder them as:
//                    //
//                    // 1. AAA
//                    // 2. BBB
//                    // 3. CCC
//                    //
//
//                    // if (end > 0) {} // #End of if (end > 0)
//
//                    //
//                    // Case 2
//                    //
//                    // There are list item spans ahead current editing
//                    int thisNumber = 1;
//                    ListNumberSpan[] aheadListItemSpans = editable.getSpans(
//                            start - 2, start - 1, ListNumberSpan.class);
//                    if (null != aheadListItemSpans
//                            && aheadListItemSpans.length > 0) {
//                        ListNumberSpan previousListItemSpan = aheadListItemSpans[aheadListItemSpans.length - 1];
//                        if (null != previousListItemSpan) {
//                            int pStart = editable
//                                    .getSpanStart(previousListItemSpan);
//                            int pEnd = editable
//                                    .getSpanEnd(previousListItemSpan);
//
//                            //
//                            // Handle this case:
//                            // 1. A
//                            // B
//                            // C
//                            // 1. D
//                            //
//                            // User puts focus to B and click List icon, to
//                            // change it to:
//                            // 2. B
//                            //
//                            // Then user puts focus to C and click List icon, to
//                            // change it to:
//                            // 3. C
//                            // For this one, we need to finish the span "2. B"
//                            // correctly
//                            // Which means we need to set the span end to a
//                            // correct value
//                            // This is doing this.
//                            if (editable.charAt(pEnd - 1) == Constants.CHAR_NEW_LINE) {
//                                editable.removeSpan(previousListItemSpan);
//                                editable.setSpan(previousListItemSpan, pStart,
//                                        pEnd - 1,
//                                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//                            }
//
//                            int previousNumber = previousListItemSpan
//                                    .getNumber();
//                            thisNumber = previousNumber + 1;
//                            makeLineAsList(thisNumber);
//                        }
//                    } else {
//                        //
//                        // Case 1
//                        thisNumber = 1;
//                        makeLineAsList(1);
//                    }
//
//                    //
//                    // Case 2
//                    //
//                    // Handle behind list item spans
//                    // reorder them
//                    // int totalLength = editable.toString().length();
//                    // if (totalLength > end) {}
//                    reNumberBehindListItemSpans(end, editable, thisNumber);
//                } else {
//                    //
//                    // Current line is list item span
//                    // By clicking the image view, we should remove the
//                    // ListItemSpan
//                    ListNumberSpan currentLineListItemSpan = listNumberSpans[0];
//                    int spanEnd = editable.getSpanEnd(currentLineListItemSpan);
//                    editable.removeSpan(currentLineListItemSpan);
//
//                    //
//                    // Change the content to trigger the editable redraw
//                    editable.insert(spanEnd, Constants.ZERO_WIDTH_SPACE_STR);
//                    editable.delete(spanEnd, spanEnd + 1);
//
//                    //
//                    // The new list should start from 1
//                    reNumberBehindListItemSpans(spanEnd, editable, 0);
//                }
            }
        });
    }

    @Override
    public void applyStyle(Editable editable, int start, int end) {
        // logAllListItems(editable, true);
        ListNumberSpan[] listSpans = editable.getSpans(start, end,
                ListNumberSpan.class);
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
                int listSpanSize = listSpans.length;
                int previousListSpanIndex = listSpanSize - 1;
                if (previousListSpanIndex > -1) {
                    ListNumberSpan previousListSpan = listSpans[previousListSpanIndex];
                    int lastListItemSpanStartPos = editable.getSpanStart(previousListSpan);
                    int lastListItemSpanEndPos = editable.getSpanEnd(previousListSpan);
                    CharSequence listItemSpanContent = editable.subSequence(
                            lastListItemSpanStartPos, lastListItemSpanEndPos);

                    if (isEmptyListItemSpan(listItemSpanContent)) {
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
                        editable.removeSpan(previousListSpan);

                        //
                        // Deletes the ZERO_WIDTH_SPACE_STR and \n
                        editable.delete(lastListItemSpanStartPos, lastListItemSpanEndPos);
                        updateCheckStatus();

                        //
                        // Restart the counting for the list item spans after
                        // previousListSpan
                        reNumberBehindListItemSpans(lastListItemSpanStartPos, editable, 0);
                        return;
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
                        if (end > lastListItemSpanStartPos) {
                            editable.removeSpan(previousListSpan);
                            editable.setSpan(previousListSpan,
                                    lastListItemSpanStartPos, end - 1,
                                    Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        }
                    }
                    int lastListItemNumber = previousListSpan.getNumber();
                    int thisNumber = lastListItemNumber + 1;
                    ListNumberSpan newListItemSpan = makeLineAsList(thisNumber);
                    end = editable.getSpanEnd(newListItemSpan);
                    reNumberBehindListItemSpans(end, editable, thisNumber);
                } // #End of if it is in ListItemSpans..
            } // #End of user types \n
        } else {
            //
            // User deletes
            int spanStart = editable.getSpanStart(listSpans[0]);
            int spanEnd = editable.getSpanEnd(listSpans[0]);
            ListNumberSpan theFirstSpan = listSpans[0];
            if (listSpans.length > 1) {
                int theFirstSpanNumber = theFirstSpan.getNumber();
                for (ListNumberSpan lns : listSpans) {
                    if (lns.getNumber() < theFirstSpanNumber) {
                        theFirstSpan = lns;
                    }
                }
                spanStart = editable.getSpanStart(theFirstSpan);
                spanEnd = editable.getSpanEnd(theFirstSpan);
            }


            if (spanStart >= spanEnd) {
                // Case 1:
                // We assume the user wants to remove the span as the last char of the span is deleted
                for (ListNumberSpan listSpan : listSpans) {
                    editable.removeSpan(listSpan);
                }

                //
                // To delete the previous span's \n
                // So the focus will go to the end of previous span
                if (spanStart > 0) {
                    editable.delete(spanStart - 1, spanEnd);
                }

                if (editable.length() > spanEnd) {
                    ListNumberSpan[] spansBehind = editable.getSpans(spanEnd, spanEnd + 1, ListNumberSpan.class);
                    if (spansBehind.length > 0) {
                        int removedNumber = theFirstSpan.getNumber();
                        reNumberBehindListItemSpans(spanStart, editable,
                                removedNumber - 1);
                    }
                }
            } else if (start == spanStart) {
                return;
            } else if (start == spanEnd) {
                // Case 3:
                // We assume the user wants to remove the span as the first char of the span is deleted
                if (editable.length() > start) {
                    if (editable.charAt(start) == Constants.CHAR_NEW_LINE) {
                        ListNumberSpan[] spans = editable.getSpans(start, start, ListNumberSpan.class);
                        if (spans.length > 0) {
                            mergeForward(editable, theFirstSpan, spanStart, spanEnd);
                        } else {
                            editable.removeSpan(spans[0]);
                        }
                    } else {
                        mergeForward(editable, theFirstSpan, spanStart, spanEnd);
                    }
                }
            } else if (start > spanStart && end < spanEnd) {
                //
                // Handle this case:
                // 1. AAA1
                // 2. BBB2
                // 3. CCC3
                //
                // User deletes '1' / '2' / '3'
                // Or any other character inside of a span
                //
                // For this case we won't need do anything
                // As we need to keep the span styles as they are
                return;
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
                int previousNumber = theFirstSpan.getNumber();
                reNumberBehindListItemSpans(end, editable, previousNumber);
            }
        }
    } // # End of applyStyle(..)

    protected void mergeForward(Editable editable, ListNumberSpan listSpan, int spanStart, int spanEnd) {
        if (editable.length() <= spanEnd + 1) {
            return;
        }
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
        reNumberBehindListItemSpans(spanEnd, editable, listSpan.getNumber());
    }

    private void updateCheckStatus() {
        mListBulletChecked = ButtonCheckStatusUtil.shouldCheckButton(getEditText(), ListNumberSpan.class);
    }

    /**
     * Check if this is an empty span.
     * <p>
     * <B>OLD COMMENT: and whether it is at the end of the spans list</B>
     *
     * @param listItemSpanContent
     * @return
     */
    private boolean isEmptyListItemSpan(CharSequence listItemSpanContent) {
        int spanLen = listItemSpanContent.length();
        if (spanLen == 2) {
            //
            // This case:
            // 1. A
            // 2.
            //
            // Line 2 is empty
            return true;
        } else {
            return false;
        }
    }

    private ListNumberSpan makeLineAsList(int num) {
        EditText editText = getEditText();
        return makeLineAsList(Util.getCurrentCursorLine(editText), num);
    }

    private ListNumberSpan makeLineAsList(int line, int num) {
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
        editable.setSpan(listItemSpan, start, end,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);

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


    /**
     * Change the selected {@link ListBulletSpan} to {@link ListNumberSpan}
     *
     * @param editable
     * @param listBulletSpans
     */
    protected void changeListBulletSpanToListNumberSpan(Editable editable,
                                                        ListBulletSpan[] listBulletSpans) {

        if (null == listBulletSpans || listBulletSpans.length == 0) {
            return;
        }


        // -
        // Handle this case:
        // User has:
        //
        // * AA
        // * BB
        // 1. CC
        // 2. DD
        //
        // Then user clicks Bullet icon at line 2:
        //
        // So it should change to:
        // * AA
        // 1. BB
        // 2. CC
        // 3. DD
        //
        // So this is for handling the line after 2nd line.
        // "CC" should be changed from 1 to 2
        //
        // - Restart the count after the bullet span
        int len = listBulletSpans.length;
        ListBulletSpan lastListBulletSpan = listBulletSpans[len - 1];

        // -- Remember the last list number span end
        // -- Because this list number span will be replaced with
        // -- ListBulletSpan after the loop, we won't be able to
        // -- get the last ListNumberSpan end after the replacement.
        // --
        // -- After this pos (lastListNumberSpanEnd), if there are
        // -- any ListNumberSpan, we would like to concat them with
        // -- our current editing : i.e.: we are changing the
        // -- ListBulletSpan to ListNumberSpan
        // -- If after the changing, the last ListNumberSpan's number
        // -- is X, then the following ListNumberSpan should starts
        // -- from X + 1.
        int lastListNumberSpanEnd = editable.getSpanEnd(lastListBulletSpan);

        //
        // - Replace all ListBulletSpan to ListNumberSpan
        //
        int previousListNumber = 0;

        //
        // Gets the previous list span number
        //
        // For handling this case:
        //
        // 1. AA
        // * BB
        //
        // When user clicks Number icon at line 2
        // It should change to:
        // 1. AA
        // 2. BB
        //
        // So the number of the new generated ListNumberSpan should
        // start from the previous ListNumberSpan
        ListBulletSpan firstListBulletSpan = listBulletSpans[0];
        int firstListBulletSpanStart = editable.getSpanStart(firstListBulletSpan);
        if (firstListBulletSpanStart > 2) {
            ListNumberSpan[] previousListNumberSpans = editable.getSpans(
                    firstListBulletSpanStart - 2,
                    firstListBulletSpanStart - 1,
                    ListNumberSpan.class);
            if (null != previousListNumberSpans && previousListNumberSpans.length > 0) {
                previousListNumber = previousListNumberSpans[previousListNumberSpans.length - 1].getNumber();
            }
        }

        for (ListBulletSpan listBulletSpan : listBulletSpans) {
            int start = editable.getSpanStart(listBulletSpan);
            int end = editable.getSpanEnd(listBulletSpan);

            editable.removeSpan(listBulletSpan);
            previousListNumber++;
            ListNumberSpan listNumberSpan = new ListNumberSpan(previousListNumber);
            editable.setSpan(listNumberSpan, start, end,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        // -- Change the content to trigger the editable redraw
        editable.insert(lastListNumberSpanEnd, Constants.ZERO_WIDTH_SPACE_STR);
        editable.delete(lastListNumberSpanEnd + 1, lastListNumberSpanEnd + 1);
        // -- End: Change the content to trigger the editable redraw

        ARE_ListNumber.reNumberBehindListItemSpans(lastListNumberSpanEnd + 1,
                editable, previousListNumber);
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