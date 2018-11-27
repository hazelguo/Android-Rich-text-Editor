package com.chinalwb.are.styles;

import android.text.Editable;
import android.text.Spanned;
import android.widget.EditText;

import com.chinalwb.are.styles.toolitems.IARE_ToolItem_Updater;

import java.lang.reflect.ParameterizedType;

public abstract class ARE_ABS_Style<E> implements IARE_Style {

    protected EditText mEditText;

    private Class<E> clazzE;

    private IARE_ToolItem_Updater mCheckUpdater;

    protected boolean mButtonChecked;

    public ARE_ABS_Style(EditText editText, IARE_ToolItem_Updater checkUpdater) {
        mEditText = editText;
        mCheckUpdater = checkUpdater;
        clazzE = (Class<E>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override
    public void applyStyle(Editable editable, int start, int end) {
        if (getIsChecked()) {
            if (end > start) {
                // User inputs or user selects a range
                checkAndMergeSpan(editable, start, end, clazzE);
            } else {
                // User deletes
                E[] spans = editable.getSpans(start, end, clazzE);
                if (spans.length > 0) {
                    E span = spans[0];
                    int lastSpanStart = editable.getSpanStart(span);
                    for (E e : spans) {
                        int lastSpanStartTmp = editable.getSpanStart(e);
                        if (lastSpanStartTmp > lastSpanStart) {
                            lastSpanStart = lastSpanStartTmp;
                            span = e;
                        }
                    }

                    int eStart = editable.getSpanStart(span);
                    int eEnd = editable.getSpanEnd(span);

                    if (eStart >= eEnd) {
                        editable.removeSpan(span);
                        extendPreviousSpan(editable, eStart);

                        updateCheckStatus(false);
                    } else {
                        //
                        // Do nothing, the default behavior is to extend
                        // the span's area.
                    }
                }
            }
        } else {
            // User un-checks the style
            if (end > start) { // User inputs or user selects a range
                int nextSpanStart = editable.nextSpanTransition(start - 1, end, clazzE);
                if (nextSpanStart < end) {
                    E[] spans = editable.getSpans(start, end, clazzE);
                    E span = spans[0];
                    if (null != span) {
                        // User stops the style, and wants to show
                        // un-UNDERLINE characters
                        int ess = editable.getSpanStart(span); // ess == existing span start
                        int ese = editable.getSpanEnd(span); // ese = existing span end
                        if (start >= ese) {
                            // User inputs to the end of the existing e span
                            // End existing e span
                            editable.removeSpan(span);
                            editable.setSpan(span, ess, start - 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        } else if (start == ess && end == ese) {
                            // Case 1 desc:
                            // *BBBBBB*
                            // All selected, and un-check e
                            editable.removeSpan(span);
                        } else if (start > ess && end < ese) {
                            // Case 2 desc:
                            // BB*BB*BB
                            // *BB* is selected, and un-check e
                            editable.removeSpan(span);
                            E spanLeft = newSpan();
                            editable.setSpan(spanLeft, ess, start, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                            E spanRight = newSpan();
                            editable.setSpan(spanRight, end, ese, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        } else if (start == ess && end < ese) {
                            // Case 3 desc:
                            // *BBBB*BB
                            // *BBBB* is selected, and un-check e
                            editable.removeSpan(span);
                            E newSpan = newSpan();
                            editable.setSpan(newSpan, end, ese, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        } else if (start > ess && end == ese) {
                            // Case 4 desc:
                            // BB*BBBB*
                            // *BBBB* is selected, and un-check e
                            editable.removeSpan(span);
                            E newSpan = newSpan();
                            editable.setSpan(newSpan, ess, start, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        }
                    }
                }
            }
            //else if (end == start) { // User deletes or changes focus position. Do nothing for this case
        }
    }

    private void checkAndMergeSpan(Editable editable, int start, int end, Class<E> clazzE) {
        E leftSpan = null;
        E[] leftSpans = editable.getSpans(start-1, start, clazzE);
        if (leftSpans.length > 0) {
            leftSpan = leftSpans[0];
        }

        E rightSpan = null;
        E[] rightSpans = editable.getSpans(end, end+1, clazzE);
        if (rightSpans.length > 0) {
            rightSpan = rightSpans[0];
        }

        int leftSpanStart = editable.getSpanStart(leftSpan);
        int rightSpanEnd = editable.getSpanEnd(rightSpan);
        int newSpanStart = start;
        int newSpanEnd = end;
        if (leftSpan != null && rightSpan != null) {
            newSpanStart = leftSpanStart;
            newSpanEnd = rightSpanEnd;
        } else if (leftSpan != null) {
            newSpanStart = leftSpanStart;
        } else if (rightSpan != null) {
            newSpanEnd = rightSpanEnd;
        }

        removeAllSpans(editable, newSpanStart, newSpanEnd, clazzE);
        editable.setSpan(newSpan(), newSpanStart, newSpanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
    }

    private void removeAllSpans(Editable editable, int start, int end, Class<E> clazzE) {
        E[] allSpans = editable.getSpans(start, end, clazzE);
        for (E span : allSpans) {
            editable.removeSpan(span);
        }
    }

    protected void updateCheckStatus(boolean newChecked) {
        boolean oldChecked = mButtonChecked;
        setChecked(newChecked);
        if (mCheckUpdater != null) {
            mCheckUpdater.onCheckStatusUpdate(oldChecked, newChecked);
        }
    }

    @Override
    public void setChecked(boolean isChecked) {
        this.mButtonChecked = isChecked;
    }

    @Override
    public boolean getIsChecked() {
        return this.mButtonChecked;
    }

    protected void extendPreviousSpan(Editable editable, int pos) {
        // Do nothing by default
    }

    public abstract E newSpan();
}
