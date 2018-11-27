package com.chinalwb.are.spans;

import android.text.style.LeadingMarginSpan;

public abstract class AreListSpan implements LeadingMarginSpan {
    public static final int MIN_DEPTH = 1;
    public static final int MAX_DEPTH = 10;

    protected static final int CONTENT_SPACING = 18;
    protected static final int HEADER_SPACING = 36;
    protected static final int HEADER_EXTRA_DIGIT_SPACING = 24;
    private static final int DEPTH_SPACING = 48;

    public enum ListType {
        OL {
            @Override
            protected void appendTagName(StringBuilder out) {
                out.append("ol");
            }
        },
        UL {
            @Override
            protected void appendTagName(StringBuilder out) {
                out.append("ul");
            }
        };

        public final void appendTag(StringBuilder out, boolean start) {
            out.append('<');
            if (!start) {
                out.append('/');
            }
            appendTagName(out);
            out.append('>');
        }

        protected abstract void appendTagName(StringBuilder out);
    }

    private int mDepth;
    private int mOrder;
    private final ListType mListType;

    public AreListSpan(int depth, int order, ListType listType) {
        setDepth(depth);
        setOrder(order);
        mListType = listType;
    }

    public int getDepth() {
        return mDepth;
    }

    public void setDepth(int depth) {
        if (depth < MIN_DEPTH) {
            mDepth = MIN_DEPTH;
        } else if (depth > MAX_DEPTH) {
            mDepth = MAX_DEPTH;
        } else {
            mDepth = depth;
        }
    }

    public void setOrder(int order) {
        mOrder = order;
    }

    public int getOrder() {
        return mOrder;
    }

    public int getIndent() {
        int depthSpacing = (mDepth - 1) * DEPTH_SPACING;
        int extraDigitSpacing = mOrder > 0 && ListType.OL.equals(getListType()) ?
                (int) Math.log10(mOrder) * HEADER_EXTRA_DIGIT_SPACING : 0;
        return CONTENT_SPACING + HEADER_SPACING + depthSpacing + extraDigitSpacing;
    }

    public ListType getListType() {
        return mListType;
    }

    @Override
    public int getLeadingMargin(boolean first) {
        return getIndent();
    }
}
