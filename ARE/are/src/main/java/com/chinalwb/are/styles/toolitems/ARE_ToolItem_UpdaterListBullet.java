package com.chinalwb.are.styles.toolitems;

import java.util.List;

public class ARE_ToolItem_UpdaterListBullet extends ARE_ToolItem_UpdaterList {
    private ARE_ToolItem_ListNumber mToolItemListNumber;

    public ARE_ToolItem_UpdaterListBullet(IARE_ToolItem toolItem) {
        super(toolItem);
        List<IARE_ToolItem> toolitems = toolItem.getToolbar().getToolItems();
        for (IARE_ToolItem item : toolitems) {
            if (item instanceof ARE_ToolItem_ListNumber) {
                mToolItemListNumber = (ARE_ToolItem_ListNumber) item;
                break;
            }
        }
    }

    @Override
    public void onCheckStatusUpdate(boolean newChecked) {
        super.onCheckStatusUpdate(newChecked);
        if (newChecked) {
            mToolItemListNumber.getToolItemUpdater().onCheckStatusUpdate(false);
        }
    }
}
