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
 * 
 */
public class ARE_ListBullet extends ARE_ABS_FreeStyle {

	private ImageView mListBulletImageView;

	private boolean mListBulletChecked;

	public ARE_ListBullet(ImageView imageView) {
		this.mListBulletImageView = imageView;
		setListenerForImageView(this.mListBulletImageView);
	}

	@Override
	public void setListenerForImageView(final ImageView imageView) {
		imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText editText = getEditText();
				Editable editable = editText.getText();
				int[] selectionLines = Util.getCurrentSelectionLines(editText);
				int start = Util.getThisLineStart(editText, selectionLines[0]);
				int end = Util.getThisLineEnd(editText, selectionLines[1]);
				//
				// Check if there is any ListNumberSpan. If so, remove existing ListNumberSpans
				// in the selection and reorder ListNumberSpans after the selection.
				//
				// For example, the current text is:
				// 1. aa
				// 2. bb
				// 3. cc
				// 4. dd
				//
				// Then the user wants to convert "bb" & "cc" to ListBulletSpan, resulting:
				// 1. aa
				// * bb
				// * cc
				// 1. dd
				//
				// Note that "dd" has been restarted from 1
				// Note that we use start & end instead of selectionStart & selectionEnd because
				// partial selection should be treated as full-line selection in bullet.
				ListNumberSpan[] listNumberSpans = editable.getSpans(start, end, ListNumberSpan.class);
				if (listNumberSpans != null && listNumberSpans.length > 0) {
					// - Restart the count after the bullet span
					int len = listNumberSpans.length;
					ListNumberSpan lastListNumberSpan = listNumberSpans[len - 1];
					int lastListNumberSpanEnd = editable.getSpanEnd(lastListNumberSpan);

					// -- Change the content to trigger the editable redraw
					editable.insert(lastListNumberSpanEnd, Constants.ZERO_WIDTH_SPACE_STR);
					editable.delete(lastListNumberSpanEnd + 1, lastListNumberSpanEnd + 1);
					// -- End: Change the content to trigger the editable redraw

					ARE_ListNumber.reNumberBehindListItemSpans(lastListNumberSpanEnd + 1, editable, 0);

					// - Remove all ListNumberSpan
					for (ListNumberSpan listNumberSpan : listNumberSpans) {
						editable.removeSpan(listNumberSpan);
					}
				}
				// If all lines have bullet spans, remove all bullet spans. Otherwise, apply bullet
				// spans to all lines.
				if (getIsChecked()) {
					ListBulletSpan[] listBulletSpans = editable.getSpans(start, end, ListBulletSpan.class);
					for (ListBulletSpan listBulletSpan : listBulletSpans) {
						editable.removeSpan(listBulletSpan);
					}
					setChecked(false);
				} else {
					for (int line = selectionLines[0]; line <= selectionLines[1]; ++line) {
						// Only add ListBulletSpan if there's no such span exists.
						// For example, the current text is:
						// * aa
						// bb
						//
						// Then the user selects both lines and clicks bullet icon. In this case, the
						// bullet span on "aa" should be kept, whereas, a bullet span should be
						// added to "bb".
						int lineStart = Util.getThisLineStart(editText, line);
						int lineEnd = Util.getThisLineEnd(editText, line);
						int nextSpanStart = editable.nextSpanTransition(lineStart - 1, lineEnd, ListBulletSpan.class);
						if (nextSpanStart >= lineEnd) {
							makeLineAsBullet(line);
						}
					}
					setChecked(true);
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
				int listSpanSize = listSpans.length;
				int previousListSpanIndex = listSpanSize - 1;
				if (previousListSpanIndex > -1) {
					ListBulletSpan previousListSpan = listSpans[previousListSpanIndex];
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
						makeLineAsBullet();
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
	} // # End of applyStyle(..)

	private void updateCheckStatus() {
		mListBulletChecked = ButtonCheckStatusUtil.shouldCheckButton(getEditText(), ListBulletSpan.class);
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
		// Util.log("merge span start == " + spanStart + " end == " + spanEnd);
	}

	private void logAllBulletListItems(Editable editable) {
		ListBulletSpan[] listItemSpans = editable.getSpans(0,
				editable.length(), ListBulletSpan.class);
		for (ListBulletSpan span : listItemSpans) {
			int ss = editable.getSpanStart(span);
			int se = editable.getSpanEnd(span);
			Util.log("List All: " + " :: start == " + ss + ", end == " + se);
		}
	}

	/**
	 * Check if this is an empty span.
	 * 
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

	private void makeLineAsBullet() {
		EditText editText = getEditText();
		makeLineAsBullet(Util.getCurrentCursorLine(editText));
	}

	private void makeLineAsBullet(int line) {
		EditText editText = getEditText();
		int start = Util.getThisLineStart(editText, line);
		Editable editable = editText.getText();
		editable.insert(start, Constants.ZERO_WIDTH_SPACE_STR);
		start = Util.getThisLineStart(editText, line);
		int end = Util.getThisLineEnd(editText, line);

		if (end < 1) {
			return;
		}
		if (editable.charAt(end - 1) == Constants.CHAR_NEW_LINE) {
			end--;
		}

		ListBulletSpan BulletListItemSpan = new ListBulletSpan();
		editable.setSpan(BulletListItemSpan, start, end,
				Spannable.SPAN_INCLUSIVE_INCLUSIVE);
	}

	@Override
	public ImageView getImageView() {
		return this.mListBulletImageView;
	}

	@Override
	public void setChecked(boolean isChecked) {
		this.mListBulletChecked = isChecked;
	}

	@Override
	public boolean getIsChecked() {
		return this.mListBulletChecked;
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
