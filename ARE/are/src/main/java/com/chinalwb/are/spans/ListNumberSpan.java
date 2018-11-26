package com.chinalwb.are.spans;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.Spanned;

public class ListNumberSpan extends AreListSpan {
    private float mWidth = -1;

  public ListNumberSpan(int depth, int order) {
      super(depth, order, ListType.OL);
  }

  @Override
  public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top,
                                int baseline, int bottom, CharSequence text, int start, int end,
                                boolean first, Layout l) {
    
      if (((Spanned) text).getSpanStart(this) == start) {
          String heading = getOrder() + ".";
          if (mWidth == -1) {
              mWidth = p.measureText(heading);
          }

          float position = getIndent() - (x + dir * (CONTENT_SPACING + mWidth));
          c.drawText(heading, position, baseline, p);
      }
  }
}