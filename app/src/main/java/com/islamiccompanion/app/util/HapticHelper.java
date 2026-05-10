package com.islamiccompanion.app.util;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.provider.Settings;

/**
 * Centralized haptic vocabulary.
 * All methods are no-ops if the system "Touch feedback" setting is disabled.
 *
 * Vocabulary:
 *  TICK     — 10 ms  — tasbih tap, ayah select, page turn
 *  CONFIRM  — 50 ms  — action done: bookmarked, copied, prayer logged
 *  SUCCESS  — 50-100-50 ms pattern — target reached, streak milestone, prayer on time
 *  ERROR    — 100-50-100 ms pattern — network error, permission denied
 *  ADHAN    — 80-40-80-40-80 ms — just before adhan audio begins
 */
public final class HapticHelper {

    private HapticHelper() {}

    /** Very short tick — single bead tap, page turn. */
    public static void tick(Context ctx) {
        vibrate(ctx, new long[]{0, 10}, -1);
    }

    /** Medium confirm — bookmark saved, copy complete. */
    public static void confirm(Context ctx) {
        vibrate(ctx, new long[]{0, 50}, -1);
    }

    /** Success pattern — tasbih set complete, prayer logged on time. */
    public static void success(Context ctx) {
        vibrate(ctx, new long[]{0, 50, 50, 100, 50, 50}, -1);
    }

    /** Error pattern — network error, permission denied. */
    public static void error(Context ctx) {
        vibrate(ctx, new long[]{0, 100, 50, 100}, -1);
    }

    /** Adhan pre-call pattern — before audio starts. */
    public static void adhanCall(Context ctx) {
        vibrate(ctx, new long[]{0, 80, 40, 80, 40, 80}, -1);
    }

    private static void vibrate(Context ctx, long[] pattern, int repeat) {
        if (!isFeedbackEnabled(ctx)) return;

        Vibrator vibrator = getVibrator(ctx);
        if (vibrator == null || !vibrator.hasVibrator()) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeat));
        } else {
            vibrator.vibrate(pattern, repeat);
        }
    }

    private static Vibrator getVibrator(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vm = (VibratorManager)
                    ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            return vm != null ? vm.getDefaultVibrator() : null;
        }
        return (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
    }

    private static boolean isFeedbackEnabled(Context ctx) {
        try {
            return Settings.System.getInt(
                    ctx.getContentResolver(),
                    Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) != 0;
        } catch (Exception e) {
            return true;
        }
    }
}
