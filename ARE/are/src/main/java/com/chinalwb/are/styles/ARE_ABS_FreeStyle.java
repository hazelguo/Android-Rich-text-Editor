package com.chinalwb.are.styles;

import android.widget.EditText;

import com.chinalwb.are.styles.toolitems.IARE_ToolItem_Updater;

public abstract class ARE_ABS_FreeStyle implements IARE_Style {

    protected EditText mEditText;
    private IARE_ToolItem_Updater mCheckUpdater;
    protected boolean mButtonChecked;

    public ARE_ABS_FreeStyle(EditText editText, IARE_ToolItem_Updater checkUpdater) {
        mEditText = editText;
        mCheckUpdater = checkUpdater;
    }

    protected void updateCheckStatus(boolean oldChecked, boolean newChecked) {
        setChecked(oldChecked);
        if (mCheckUpdater != null) {
            mCheckUpdater.onCheckStatusUpdate(oldChecked, newChecked);
        }
    }

    protected void updateCheckStatus(boolean newChecked) {
        updateCheckStatus(mButtonChecked, newChecked);
    }

    @Override
    public void setChecked(boolean isChecked) {
        this.mButtonChecked = isChecked;
    }

    @Override
    public boolean getIsChecked() {
        return this.mButtonChecked;
    }
}
