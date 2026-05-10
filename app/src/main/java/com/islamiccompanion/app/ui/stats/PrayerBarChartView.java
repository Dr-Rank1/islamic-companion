package com.islamiccompanion.app.ui.stats;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.islamiccompanion.app.R;

/**
 * Simple canvas-based bar chart showing the last 7 days of prayer completion.
 * Each bar represents one day; height = number of prayers prayed (0–5).
 */
public class PrayerBarChartView extends View {

    private int[] data = new int[7];
    private final Paint barPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bgPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final int MAX_PRAYERS = 5;
    private static final String[] DAY_LABELS = {"–6", "–5", "–4", "–3", "–2", "–1", "T"};

    public PrayerBarChartView(Context context) { super(context); init(context); }
    public PrayerBarChartView(Context context, AttributeSet a) { super(context, a); init(context); }
    public PrayerBarChartView(Context context, AttributeSet a, int s) { super(context, a, s); init(context); }

    private void init(Context ctx) {
        barPaint.setColor(ContextCompat.getColor(ctx, R.color.primary));
        bgPaint.setColor(ContextCompat.getColor(ctx, R.color.stat_bar_bg));
        labelPaint.setColor(ContextCompat.getColor(ctx, R.color.text_secondary));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(28f);
    }

    public void setData(int[] counts) {
        if (counts != null && counts.length == 7) {
            this.data = counts.clone();
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        int n = data.length;
        float gap = 8f;
        float barW = (w - gap * (n + 1)) / n;
        float labelH = 36f;
        float chartH = h - labelH - gap;

        for (int i = 0; i < n; i++) {
            float left = gap + i * (barW + gap);
            float right = left + barW;

            // Background track
            RectF bg = new RectF(left, gap, right, gap + chartH);
            canvas.drawRoundRect(bg, 8, 8, bgPaint);

            // Filled bar
            int count = Math.min(data[i], MAX_PRAYERS);
            if (count > 0) {
                float fillH = (chartH * count) / MAX_PRAYERS;
                RectF bar = new RectF(left, gap + chartH - fillH, right, gap + chartH);
                // Colour intensity based on completeness
                int alpha = 128 + (int) (127 * count / (float) MAX_PRAYERS);
                barPaint.setAlpha(alpha);
                canvas.drawRoundRect(bar, 8, 8, barPaint);
            }

            // Day label
            float cx = left + barW / 2;
            canvas.drawText(DAY_LABELS[i], cx, h - 4, labelPaint);
        }
    }
}
