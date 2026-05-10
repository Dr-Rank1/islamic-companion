package com.islamiccompanion.app.ui.tasbih;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.islamiccompanion.app.R;

/**
 * Custom view that renders a row of prayer beads (misbaha).
 * Shows filled beads for completed counts, hollow for remaining.
 */
public class BeadView extends View {

    private int totalBeads  = 33;
    private int filledBeads = 0;

    private final Paint filledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emptyPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public BeadView(Context ctx) { super(ctx); init(ctx); }
    public BeadView(Context ctx, AttributeSet a) { super(ctx, a); init(ctx); }
    public BeadView(Context ctx, AttributeSet a, int s) { super(ctx, a, s); init(ctx); }

    private void init(Context ctx) {
        filledPaint.setStyle(Paint.Style.FILL);
        filledPaint.setColor(ContextCompat.getColor(ctx, R.color.accent));

        emptyPaint.setStyle(Paint.Style.STROKE);
        emptyPaint.setColor(ContextCompat.getColor(ctx, R.color.primary_tint_medium));
        emptyPaint.setStrokeWidth(2f);

        stringPaint.setStyle(Paint.Style.STROKE);
        stringPaint.setColor(ContextCompat.getColor(ctx, R.color.divider));
        stringPaint.setStrokeWidth(1.5f);
    }

    public void setTotalBeads(int total) {
        this.totalBeads = Math.max(1, total);
        invalidate();
    }

    public void setFilledBeads(int filled) {
        this.filledBeads = Math.min(filled, totalBeads);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();
        float cy = h / 2f;

        if (totalBeads <= 0) return;

        float beadR  = Math.min(h / 2f - 4f, 16f);
        float spacing = (w - 2 * beadR) / (float) (totalBeads - 1 > 0 ? totalBeads - 1 : 1);

        // Connecting string
        canvas.drawLine(beadR, cy, w - beadR, cy, stringPaint);

        // Draw beads
        for (int i = 0; i < totalBeads; i++) {
            float cx = beadR + i * spacing;
            if (i < filledBeads) {
                canvas.drawCircle(cx, cy, beadR, filledPaint);
            } else {
                canvas.drawCircle(cx, cy, beadR, emptyPaint);
            }
        }
    }
}
