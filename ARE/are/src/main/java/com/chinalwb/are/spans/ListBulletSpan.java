package com.chinalwb.are.spans;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.text.Layout;
import android.text.Spanned;

public class ListBulletSpan extends AreListSpan {
	private static final int BULLET_RADIUS = 6;
	private static final int X_OFFSET;

	private static Path sBulletPath = null;
	private float mYOffset = -1;

	static {
		X_OFFSET = CONTENT_SPACING + BULLET_RADIUS;
	}

	public ListBulletSpan(int depth, int order) {
		super(depth, order, ListType.UL);
	}

	@Override
	public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top,
			int baseline, int bottom, CharSequence text, int start, int end,
			boolean first, Layout l) {

		if (((Spanned) text).getSpanStart(this) == start) {
			if (mYOffset == -1) {
				Rect rect = new Rect();
				p.getTextBounds("A", 0, 1, rect);
				mYOffset = rect.height() / 2.0f;
			}

			int position = getIndent() - (x + (dir * X_OFFSET));
			if (c.isHardwareAccelerated()) {
				if (sBulletPath == null) {
					sBulletPath = new Path();
					// Bullet is slightly better to avoid aliasing artifacts on mdpi devices.
					sBulletPath.addCircle(0.0f, 0.0f, BULLET_RADIUS, Path.Direction.CW);
				}

				c.save();
				c.translate(position, baseline - mYOffset);
				c.drawPath(sBulletPath, p);
				c.restore();
			} else {
				c.drawCircle(position, baseline - mYOffset, BULLET_RADIUS, p);
			}
		}
	}
}
