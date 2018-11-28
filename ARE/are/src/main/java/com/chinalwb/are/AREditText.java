package com.chinalwb.are;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import com.chinalwb.are.styles.IARE_Style;
import com.chinalwb.are.styles.toolbar.IARE_Toolbar;
import com.chinalwb.are.styles.toolitems.IARE_ToolItem;

import java.util.ArrayList;
import java.util.List;

/**
 * All Rights Reserved.
 *
 * @author Wenbin Liu
 */
public class AREditText extends AppCompatEditText {

    private IARE_Toolbar mToolbar;

    private static List<IARE_Style> sStylesList = new ArrayList<>();

    private Context mContext;

    public AREditText(Context context) {
        this(context, null);
    }

    public AREditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AREditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initGlobalValues();
        init();
        setupListener();
    }

    private void initGlobalValues() {
        int[] wh = Util.getScreenWidthAndHeight(mContext);
        Constants.SCREEN_WIDTH = wh[0];
        Constants.SCREEN_HEIGHT = wh[1];
    }

    private void init() {
        this.setFocusableInTouchMode(true);
    }

    /**
     * Sets up listeners for controls.
     */
    private void setupListener() {
        setupTextWatcher();
    } // #End of setupListener()

    /**
     * Monitoring text changes.
     */
    private void setupTextWatcher() {
        TextWatcher textWatcher = new TextWatcher() {

            int startPos = 0;
            int endPos = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                this.startPos = start;
                this.endPos = start + count;
            }

            @Override
            public void afterTextChanged(Editable s) {
                for (IARE_Style style : sStylesList) {
                    style.applyStyle(s, startPos, endPos);
                }
            }
        };

        this.addTextChangedListener(textWatcher);
    }

    public void setToolbar(IARE_Toolbar toolbar) {
        sStylesList.clear();
        this.mToolbar = toolbar;
        this.mToolbar.setEditText(this);
        List<IARE_ToolItem> toolItems = toolbar.getToolItems();
        for (IARE_ToolItem toolItem : toolItems) {
            IARE_Style style = toolItem.getStyle();
            sStylesList.add(style);
        }
    }

    @Override
    public void onSelectionChanged(int selStart, int selEnd) {
        if (mToolbar == null) {
            return;
        }
        List<IARE_ToolItem> toolItems = mToolbar.getToolItems();
        for (IARE_ToolItem toolItem : toolItems) {
            toolItem.onSelectionChanged(selStart, selEnd);
        }
    } // #End of method:: onSelectionChanged
}
