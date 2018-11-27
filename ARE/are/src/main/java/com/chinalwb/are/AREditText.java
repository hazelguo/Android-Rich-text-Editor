package com.chinalwb.are;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;

import com.chinalwb.are.android.inner.Html;
import com.chinalwb.are.render.AreTagHandler;
import com.chinalwb.are.styles.IARE_Style;
import com.chinalwb.are.styles.toolbar.IARE_Toolbar;
import com.chinalwb.are.styles.toolitems.IARE_ToolItem;

import java.util.ArrayList;
import java.util.List;

/**
 * All Rights Reserved.
 *
 * @author Wenbin Liu
 *
 */
public class AREditText extends AppCompatEditText {

    private IARE_Toolbar mToolbar;

	private static boolean MONITORING = true;

	private static List<IARE_Style> sStylesList = new ArrayList<>();

	private Context mContext;

	private TextWatcher mTextWatcher;

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
		mTextWatcher = new TextWatcher() {

			int startPos = 0;
			int endPos = 0;

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				if (!MONITORING) {
					return;
				}
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (!MONITORING) {
					return;
				}
				this.startPos = start;
				this.endPos = start + count;
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (!MONITORING) {
					return;
				}

				for (IARE_Style style : sStylesList) {
					style.applyStyle(s, startPos, endPos);
				}
			}
		};

		this.addTextChangedListener(mTextWatcher);
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

    /**
     * Sets html content to EditText.
     */
    public void fromHtml(String html) {
        Html.sContext = mContext;
        Html.TagHandler tagHandler = new AreTagHandler();
        Spanned spanned = Html.fromHtml(html, Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH, tagHandler);
        AREditText.stopMonitor();
        this.getEditableText().append(spanned);
        AREditText.startMonitor();
    }

	public String getHtml() {
		StringBuffer html = new StringBuffer();
		html.append("<html><body>");
		String editTextHtml = Html.toHtml(getEditableText(), Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL);
		html.append(editTextHtml);
		html.append("</body></html>");
		String htmlContent = html.toString().replaceAll(Constants.ZERO_WIDTH_SPACE_STR_ESCAPE, "");
		System.out.println(htmlContent);
		return htmlContent;
	}

	/**
	 * Needs this because of this bug in Android O:
	 * https://issuetracker.google.com/issues/67102093
	 */
	public void useSoftwareLayerOnAndroid8() {
		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
			this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
	}

	public static void startMonitor() {
		MONITORING = true;
	}

	public static void stopMonitor() {
		MONITORING = false;
	}
}
