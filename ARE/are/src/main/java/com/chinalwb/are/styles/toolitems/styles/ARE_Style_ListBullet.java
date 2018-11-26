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
import com.chinalwb.are.spans.AreListSpan;
import com.chinalwb.are.spans.ListBulletSpan;
import com.chinalwb.are.spans.ListNumberSpan;
import com.chinalwb.are.styles.ARE_ABS_FreeStyle;
import com.chinalwb.are.styles.ARE_ListNumber;
import com.chinalwb.are.styles.toolitems.IARE_ToolItem_Updater;

import static com.chinalwb.are.Util.addZeroWidthSpaceStrSafe;
import static com.chinalwb.are.Util.isEmptyListItemSpan;

/**
 * All Rights Reserved.
 * 
 * @author Wenbin Liu
 * 
 */
public class ARE_Style_ListBullet extends ARE_ABS_FreeStyle {

	private AREditText mEditText;

	private ImageView mListBulletImageView;

	private IARE_ToolItem_Updater mCheckUpdater;

	private boolean mListBulletChecked;

	public ARE_Style_ListBullet(AREditText editText, ImageView imageView, IARE_ToolItem_Updater checkUpdater) {
		super(editText.getContext());
		this.mEditText = editText;
		this.mListBulletImageView = imageView;
		this.mCheckUpdater = checkUpdater;
		setListenerForImageView(this.mListBulletImageView);
	}

	@Override
	public EditText getEditText() {
		return this.mEditText;
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
					updateCheckStatus(false);
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
					updateCheckStatus(true);
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

		updateCheckStatus();
	} // # End of applyStyle(..)

	private void updateCheckStatus() {
		updateCheckStatus(ButtonCheckStatusUtil.shouldCheckButton(getEditText(), ListBulletSpan.class));
	}

	private void updateCheckStatus(boolean isChecked) {
		setChecked(isChecked);
		if (mCheckUpdater != null) {
			mCheckUpdater.onCheckStatusUpdate(isChecked);
		}
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
		// AREditTextUtil.log("merge span start == " + spanStart + " end == " + spanEnd);
	}

	private void makeLineAsBullet() {
		EditText editText = getEditText();
		makeLineAsBullet(Util.getCurrentCursorLine(editText));
	}

	private void makeLineAsBullet(int line) {
		EditText editText = getEditText();
		Editable editable = editText.getText();
		int start = Util.getThisLineStart(editText, line);
		AreListSpan[] aheadListSpans = editable.getSpans(start - 2, start - 1, AreListSpan.class);
		addZeroWidthSpaceStrSafe(editable, start);
		start = Util.getThisLineStart(editText, line);
		int end = Util.getThisLineEnd(editText, line);

		if (end < 1) {
			return;
		}
		if (editable.charAt(end - 1) == Constants.CHAR_NEW_LINE) {
			end--;
		}

		ListBulletSpan BulletListItemSpan = new ListBulletSpan(
				aheadListSpans == null || aheadListSpans.length == 0 ?
						1 : aheadListSpans[0].getDepth(),
				0);
		editable.setSpan(BulletListItemSpan, start, end,
				Spannable.SPAN_INCLUSIVE_INCLUSIVE);
	}

	@Override
	public ImageView getImageView() {
		return this.mListBulletImageView;
	}

	@Override
	public void setChecked(boolean isChecked) {
		mListBulletChecked = isChecked;
	}

	@Override
	public boolean getIsChecked() {
		return mListBulletChecked;
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
