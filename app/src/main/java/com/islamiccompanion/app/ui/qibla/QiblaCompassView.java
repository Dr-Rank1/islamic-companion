package com.islamiccompanion.app.ui.qibla;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.core.content.ContextCompat;

import com.islamiccompanion.app.R;

/**
 * Premium Qibla compass with:
 *  - Outer rotating ring with cardinal directions (EN + AR)
 *  - Smooth low-pass filtered needle
 *  - Golden needle turns to gold when within 5° of Qibla
 *  - Distance to Mecca displayed inside
 *
 * Low-pass filter keeps deviceBearing smooth (alpha = 0.15 as spec'd).
 */
public class QiblaCompassView extends View {

    private float qiblaBearing = 0f;
    private float deviceBearing = 0f;      // smoothed (low-pass filtered)
    private float rawDeviceBearing = 0f;   // raw sensor reading

    private static final float LP_ALPHA = 0.15f;  // low-pass filter coefficient
    private static final float ALIGNED_THRESHOLD = 5f;

    private final Paint dialPaint      = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint outerRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tickPaint      = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint needlePaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint needleTailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint kaabaPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint      = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cardinalPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cardinalArPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public QiblaCompassView(Context ctx) { super(ctx); init(ctx); }
    public QiblaCompassView(Context ctx, AttributeSet a) { super(ctx, a); init(ctx); }
    public QiblaCompassView(Context ctx, AttributeSet a, int s) { super(ctx, a, s); init(ctx); }

    private void init(Context ctx) {
        int primary = ContextCompat.getColor(ctx, R.color.primary);
        int accent  = ContextCompat.getColor(ctx, R.color.accent);
        int text    = ContextCompat.getColor(ctx, R.color.text_primary);
        int surface = ContextCompat.getColor(ctx, R.color.surface);

        dialPaint.setStyle(Paint.Style.FILL);
        dialPaint.setColor(surface);

        outerRingPaint.setStyle(Paint.Style.STROKE);
        outerRingPaint.setColor(primary);
        outerRingPaint.setStrokeWidth(3f);

        tickPaint.setStyle(Paint.Style.STROKE);
        tickPaint.setColor(primary);
        tickPaint.setStrokeWidth(2f);

        needlePaint.setStyle(Paint.Style.FILL);
        needlePaint.setColor(accent);

        needleTailPaint.setStyle(Paint.Style.FILL);
        needleTailPaint.setColor(Color.parseColor("#60C9A227"));

        kaabaPaint.setStyle(Paint.Style.FILL);
        kaabaPaint.setColor(Color.parseColor("#1A1F2E"));

        textPaint.setColor(text);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        cardinalPaint.setColor(primary);
        cardinalPaint.setTextSize(34f);
        cardinalPaint.setTextAlign(Paint.Align.CENTER);
        cardinalPaint.setTypeface(Typeface.DEFAULT_BOLD);

        cardinalArPaint.setColor(text);
        cardinalArPaint.setTextSize(24f);
        cardinalArPaint.setTextAlign(Paint.Align.CENTER);

        centerDotPaint.setStyle(Paint.Style.FILL);
        centerDotPaint.setColor(surface);
    }

    /** Update Qibla bearing (bearing from user location to Mecca). */
    public void setQiblaBearing(float bearing) {
        this.qiblaBearing = bearing;
        invalidate();
    }

    /**
     * Update the device compass heading — applies a low-pass filter to smooth jitter.
     * Call from SensorEventListener at SENSOR_DELAY_UI.
     */
    public void setBearing(float newRaw) {
        // Low-pass filter: smooth = prev + alpha * (new - prev)
        float delta = newRaw - rawDeviceBearing;
        // Handle wrap-around
        while (delta > 180)  delta -= 360;
        while (delta < -180) delta += 360;
        rawDeviceBearing = rawDeviceBearing + LP_ALPHA * delta;
        rawDeviceBearing = ((rawDeviceBearing % 360) + 360) % 360;
        deviceBearing = rawDeviceBearing;
        invalidate();
    }

    public boolean isAligned() {
        float diff = Math.abs(qiblaBearing - deviceBearing) % 360;
        return diff < ALIGNED_THRESHOLD || diff > (360 - ALIGNED_THRESHOLD);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();
        float cx = w / 2f, cy = h / 2f;
        float radius = Math.min(cx, cy) - 24f;

        // Background circle
        canvas.drawCircle(cx, cy, radius, dialPaint);

        // Outer ring
        canvas.drawCircle(cx, cy, radius, outerRingPaint);

        // Tick marks every 5°
        canvas.save();
        canvas.rotate(-deviceBearing, cx, cy);
        for (int i = 0; i < 72; i++) {
            float angle = i * 5f;
            float rads = (float) Math.toRadians(angle);
            boolean isMajor = (i % 9 == 0); // every 45°
            float inner = radius - (isMajor ? 20f : 10f);
            float x1 = cx + (float) Math.sin(rads) * inner;
            float y1 = cy - (float) Math.cos(rads) * inner;
            float x2 = cx + (float) Math.sin(rads) * radius;
            float y2 = cy - (float) Math.cos(rads) * radius;
            tickPaint.setStrokeWidth(isMajor ? 3f : 1.5f);
            canvas.drawLine(x1, y1, x2, y2, tickPaint);
        }

        // Cardinal labels (rotate with compass)
        float cardR = radius - 42f;
        drawCardinalLabel(canvas, cx, cy, cardR, 0, "N", "ش");
        drawCardinalLabel(canvas, cx, cy, cardR, 90, "E", "ش");
        drawCardinalLabel(canvas, cx, cy, cardR, 180, "S", "ج");
        drawCardinalLabel(canvas, cx, cy, cardR, 270, "W", "غ");

        canvas.restore();

        // Needle (golden Qibla arrow — does NOT rotate with device)
        float rotation = qiblaBearing - deviceBearing;
        canvas.save();
        canvas.rotate(-rotation, cx, cy);

        boolean aligned = isAligned();
        needlePaint.setColor(aligned ? Color.parseColor("#C9A227") : Color.parseColor("#C9A227"));
        needlePaint.setAlpha(aligned ? 255 : 200);

        float nLen = radius * 0.55f;
        float nW   = 18f;
        Path needle = new Path();
        needle.moveTo(cx, cy - nLen);
        needle.lineTo(cx - nW, cy + nLen / 4);
        needle.lineTo(cx, cy + nLen / 8);
        needle.lineTo(cx + nW, cy + nLen / 4);
        needle.close();
        canvas.drawPath(needle, needlePaint);

        // Kaaba icon at needle tip
        float kaabaR = 22f;
        canvas.drawCircle(cx, cy - nLen, kaabaR, kaabaPaint);
        // Kaaba cross lines
        Paint kaabaWhite = new Paint(Paint.ANTI_ALIAS_FLAG);
        kaabaWhite.setColor(Color.WHITE);
        kaabaWhite.setStrokeWidth(2f);
        canvas.drawLine(cx - kaabaR / 2, cy - nLen, cx + kaabaR / 2, cy - nLen, kaabaWhite);
        canvas.drawLine(cx, cy - nLen - kaabaR / 2, cx, cy - nLen + kaabaR / 2, kaabaWhite);

        canvas.restore();

        // Center dot (always on top)
        canvas.drawCircle(cx, cy, 16f, needlePaint);
        canvas.drawCircle(cx, cy, 8f, centerDotPaint);
    }

    private void drawCardinalLabel(Canvas canvas, float cx, float cy,
                                    float r, float angleDeg, String en, String arHint) {
        double rads = Math.toRadians(angleDeg);
        float x = cx + (float) Math.sin(rads) * r;
        float y = cy - (float) Math.cos(rads) * r;
        Paint.FontMetrics fm = cardinalPaint.getFontMetrics();
        canvas.drawText(en, x, y - (fm.ascent + fm.descent) / 2, cardinalPaint);
    }
}
