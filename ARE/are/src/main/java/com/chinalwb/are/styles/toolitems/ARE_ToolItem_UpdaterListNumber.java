package com.chinalwb.are.styles.toolitems;

import java.util.List;

public class ARE_ToolItem_UpdaterListNumber extends ARE_ToolItem_UpdaterList {
    private ARE_ToolItem_ListBullet mToolItemListBullet;

    public ARE_ToolItem_UpdaterListNumber(IARE_ToolItem toolItem, int checkedColor, int uncheckedColor) {
        super(toolItem, checkedColor, uncheckedColor);
        List<IARE_ToolItem> toolitems = toolItem.getToolbar().getToolItems();
        for (IARE_ToolItem item : toolitems) {
            if (item instanceof ARE_ToolItem_ListBullet) {
                mToolItemListBullet = (ARE_ToolItem_ListBullet) item;
                break;
            }
        }
    }

    @Override
    public void onCheckStatusUpdate(boolean newChecked) {
        super.onCheckStatusUpdate(newChecked);
        if (newChecked) {
            mToolItemListBullet.getToolItemUpdater().onCheckStatusUpdate(false);
        }
    }
}
