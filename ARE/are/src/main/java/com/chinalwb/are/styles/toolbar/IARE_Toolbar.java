package com.chinalwb.are.styles.toolbar;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.styles.toolitems.IARE_ToolItem;

import java.util.List;

/**
 * Created by wliu on 13/08/2018.
 */

public interface IARE_Toolbar {

    void addToolbarItem(IARE_ToolItem toolbarItem);

    List<IARE_ToolItem> getToolItems();

    void setEditText(AREditText editText);

    AREditText getEditText();
}
