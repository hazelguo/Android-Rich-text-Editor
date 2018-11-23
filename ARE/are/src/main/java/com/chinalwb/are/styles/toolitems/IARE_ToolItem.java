package com.chinalwb.are.styles.toolitems;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.styles.IARE_Style;
import com.chinalwb.are.styles.toolbar.IARE_Toolbar;

/**
 * Created by wliu on 13/08/2018.
 */

public interface IARE_ToolItem {

    /**
     * Each tool item is a style, and a style combines with specific span.
     */
    public IARE_Style getStyle();

    /**
     * Each tool item has a view. If context is null, return the generated view.
     */
    public View getView(Context context);

    /**
     * Selection changed call back. Update tool item checked status
     */
    public void onSelectionChanged(int selStart, int selEnd);

    /**
     * Returns the toolbar of this tool item.
     */
    public IARE_Toolbar getToolbar();

    /**
     * Sets the toolbar for this tool item.
     */
    public void setToolbar(IARE_Toolbar toolbar);

    /**
     * Gets the tool item updater instance, will be called when style being checked and unchecked.
     */
    public IARE_ToolItem_Updater getToolItemUpdater();

    /**
     * Sets the tool item updater.
     */
    public void setToolItemUpdater(IARE_ToolItem_Updater toolItemUpdater);

    public void onActivityResult(int requestCode, int resultCode, Intent data);

}
