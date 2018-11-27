package com.chinalwb.are.styles;

import android.content.Context;
import android.widget.EditText;

public abstract class ARE_ABS_FreeStyle implements IARE_Style {

	protected Context mContext;
	protected EditText mEditText;

	public ARE_ABS_FreeStyle(Context context, EditText editText) {
		mContext = context;
		mEditText = editText;
	}

	public ARE_ABS_FreeStyle(EditText editText) {
		this(editText.getContext(), editText);
	}

	public EditText getEditText() {
		return mEditText;
	}

	// Dummy implementation
	@Override
	public boolean getIsChecked() {
		return false;
	}
}
