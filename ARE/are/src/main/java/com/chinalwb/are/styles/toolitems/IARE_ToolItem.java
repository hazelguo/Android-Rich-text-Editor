package com.chinalwb.are.styles.toolitems;

import android.content.Context;
import android.view.View;

import com.chinalwb.are.styles.IARE_Style;
import com.chinalwb.are.styles.toolbar.IARE_Toolbar;

/**
 * Created by wliu on 13/08/2018.
 */

public interface IARE_ToolItem {

    /**
     * Each tool item is a style, and a style combines with specific span.
     */
    IARE_Style getStyle();

    /**
     * Each tool item has a view. If context is null, return the generated view.
     */
    View getView(Context context);

    /**
     * Selection changed call back. Update tool item checked status
     */
    void onSelectionChanged(int selStart, int selEnd);

    /**
     * Returns the toolbar of this tool item.
     */
    IARE_Toolbar getToolbar();

    /**
     * Sets the toolbar for this tool item.
     */
    void setToolbar(IARE_Toolbar toolbar);

    /**
     * Gets the tool item updater instance, will be called when style being checked and unchecked.
     */
    IARE_ToolItem_Updater getToolItemUpdater();
}
