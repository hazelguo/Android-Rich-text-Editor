package com.chinalwb.are.styles.toolitems;

public interface IARE_ToolItem_Updater {
    void onCheckStatusUpdate(boolean checked);

    void onCheckStatusUpdate(boolean oldChecked, boolean newChecked);
}
