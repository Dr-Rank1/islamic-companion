package com.islamiccompanion.app.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.islamiccompanion.app.service.AlarmReceiver;

/**
 * Schedules exact alarms via AlarmManager as a redundant backup to WorkManager.
 * Guarantees the next prayer fires even if WorkManager is killed by aggressive OEMs.
 */
public final class AlarmHelper {

    private static final String TAG = "AlarmHelper";
    public static final String EXTRA_PRAYER_NAME = "prayer_name";

    private AlarmHelper() {}

    /**
     * Set an exact alarm that fires at {@code triggerAtMs} and launches {@link AlarmReceiver}
     * with the given prayer name.
     */
    public static void scheduleExact(Context ctx, String prayerName, long triggerAtMs) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Intent intent = new Intent(ctx, AlarmReceiver.class);
        intent.putExtra(EXTRA_PRAYER_NAME, prayerName);
        intent.setAction("com.islamiccompanion.app.PRAYER_ALARM_" + prayerName);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pi = PendingIntent.getBroadcast(ctx,
                prayerName.hashCode(), intent, flags);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pi);
                } else {
                    // Fallback — won't be exact but better than nothing
                    am.set(AlarmManager.RTC_WAKEUP, triggerAtMs, pi);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pi);
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerAtMs, pi);
            }
            Log.d(TAG, "Scheduled exact alarm for " + prayerName + " at " + triggerAtMs);
        } catch (SecurityException e) {
            Log.w(TAG, "Could not schedule exact alarm: " + e.getMessage());
        }
    }

    public static void cancel(Context ctx, String prayerName) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        intent.setAction("com.islamiccompanion.app.PRAYER_ALARM_" + prayerName);
        PendingIntent pi = PendingIntent.getBroadcast(ctx,
                prayerName.hashCode(), intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pi != null) am.cancel(pi);
    }
}
