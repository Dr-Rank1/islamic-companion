package com.islamiccompanion.app.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.islamiccompanion.app.MainActivity;
import com.islamiccompanion.app.data.prefs.AppPreferences;
import com.islamiccompanion.app.util.AlarmHelper;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * WorkManager worker for scheduling prayer-time notifications.
 *
 * Scheduling strategy (v2 — hybrid):
 *   1. WorkManager schedules workers with the prayer delay.
 *   2. AlarmManager.setExactAndAllowWhileIdle() is set as a redundant backup for
 *      the *next* prayer only — guarantees firing even on aggressive OEM ROMs.
 *   3. Re-schedules tomorrow's prayers just after midnight.
 */
public class AdhanWorker extends Worker {

    private static final String TAG = "AdhanWorker";
    public static final String CHANNEL_ID = "adhan_channel";
    public static final String EXTRA_PRAYER_NAME = "prayer_name";

    public AdhanWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        String prayerName = getInputData().getString(EXTRA_PRAYER_NAME);

        if (prayerName == null || prayerName.isEmpty()) {
            scheduleTodaysPrayers(ctx);
            scheduleTomorrowReschedule(ctx);
            return Result.success();
        }

        triggerAdhan(ctx, prayerName);
        return Result.success();
    }

    // ---- Public scheduling entry points ----

    /** Called on app start, boot, and midnight reschedule. */
    public static void scheduleTodaysPrayers(Context ctx) {
        AppPreferences prefs = new AppPreferences(ctx);
        double lat = prefs.getLastLatitude();
        double lng = prefs.getLastLongitude();

        Map<String, Integer> adj = new HashMap<>();
        adj.put("fajr",    prefs.getInt(AppPreferences.KEY_ADJ_FAJR,    0));
        adj.put("dhuhr",   prefs.getInt(AppPreferences.KEY_ADJ_DHUHR,   0));
        adj.put("asr",     prefs.getInt(AppPreferences.KEY_ADJ_ASR,     0));
        adj.put("maghrib", prefs.getInt(AppPreferences.KEY_ADJ_MAGHRIB, 0));
        adj.put("isha",    prefs.getInt(AppPreferences.KEY_ADJ_ISHA,    0));

        PrayerTimeCalculator calc = new PrayerTimeCalculator(
                lat, lng, prefs.getCalculationMethod(), prefs.getMadhab(), adj);
        Map<String, Date> prayers = calc.getTodayPrayerMap();

        long now = System.currentTimeMillis();
        int reminderMins = prefs.getReminderMinutes();
        boolean useHighReliability = prefs.getBoolean(AppPreferences.KEY_HIGH_RELIABILITY, true);

        // Cancel previous exact alarms
        for (String name : new String[]{"Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"}) {
            AlarmHelper.cancel(ctx, name);
        }

        boolean firstUpcoming = true;
        for (Map.Entry<String, Date> entry : prayers.entrySet()) {
            String name = entry.getKey();
            if ("Sunrise".equals(name)) continue;
            long prayerMs = entry.getValue().getTime();
            long fireAt   = prayerMs - (reminderMins * 60_000L);
            if (fireAt > now) {
                long delayMs = fireAt - now;
                enqueueAdhanWork(ctx, name, delayMs);

                // AlarmManager backup — only for the very next prayer (firstUpcoming)
                if (useHighReliability) {
                    AlarmHelper.scheduleExact(ctx, name, fireAt);
                    firstUpcoming = false; // set all of them for max reliability
                }
            }
        }
        Log.d(TAG, "Scheduled today's prayers (hybrid)");
    }

    private static void enqueueAdhanWork(Context ctx, String prayerName, long delayMs) {
        androidx.work.Data data = new androidx.work.Data.Builder()
                .putString(EXTRA_PRAYER_NAME, prayerName)
                .build();

        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(AdhanWorker.class)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("adhan_" + prayerName.toLowerCase())
                .build();

        WorkManager.getInstance(ctx).enqueueUniqueWork(
                "adhan_" + prayerName.toLowerCase(),
                ExistingWorkPolicy.REPLACE,
                req
        );
    }

    private static void scheduleTomorrowReschedule(Context ctx) {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        tomorrow.set(Calendar.HOUR_OF_DAY, 0);
        tomorrow.set(Calendar.MINUTE, 1);
        tomorrow.set(Calendar.SECOND, 0);
        long delayMs = tomorrow.getTimeInMillis() - System.currentTimeMillis();

        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(AdhanWorker.class)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .addTag("adhan_reschedule")
                .build();

        WorkManager.getInstance(ctx).enqueueUniqueWork(
                "adhan_reschedule", ExistingWorkPolicy.REPLACE, req);
    }

    // ---- Adhan trigger ----

    private void triggerAdhan(Context ctx, String prayerName) {
        AppPreferences prefs = new AppPreferences(ctx);
        String notifKey = getNotifPrefKey(prayerName);
        String mode = prefs.getNotifMode(notifKey);

        switch (mode) {
            case "off":
                return;
            case "silent":
                showSilentNotification(ctx, prayerName);
                return;
            case "vibrate":
                showVibratingNotification(ctx, prayerName);
                return;
            default:
                startAdhanPlayback(ctx, prayerName);
                break;
        }
    }

    private void startAdhanPlayback(Context ctx, String prayerName) {
        Intent intent = new Intent(ctx, AdhanPlaybackService.class);
        intent.putExtra(EXTRA_PRAYER_NAME, prayerName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startForegroundService(intent);
        } else {
            ctx.startService(intent);
        }
    }

    private void showSilentNotification(Context ctx, String prayerName) {
        ensureNotificationChannel(ctx);
        PendingIntent openPi = buildOpenIntent(ctx);
        androidx.core.app.NotificationCompat.Builder b =
                new androidx.core.app.NotificationCompat.Builder(ctx, CHANNEL_ID)
                        .setSmallIcon(com.islamiccompanion.app.R.drawable.ic_notification)
                        .setContentTitle(ctx.getString(
                                com.islamiccompanion.app.R.string.notif_time_for_prayer, prayerName))
                        .setContentText(ctx.getString(
                                com.islamiccompanion.app.R.string.notif_tap_to_open))
                        .setContentIntent(openPi)
                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);
        NotificationManager nm = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(prayerName.hashCode(), b.build());
    }

    private void showVibratingNotification(Context ctx, String prayerName) {
        ensureNotificationChannel(ctx);
        PendingIntent openPi = buildOpenIntent(ctx);
        androidx.core.app.NotificationCompat.Builder b =
                new androidx.core.app.NotificationCompat.Builder(ctx, CHANNEL_ID)
                        .setSmallIcon(com.islamiccompanion.app.R.drawable.ic_notification)
                        .setContentTitle(ctx.getString(
                                com.islamiccompanion.app.R.string.notif_time_for_prayer, prayerName))
                        .setContentText(ctx.getString(
                                com.islamiccompanion.app.R.string.notif_tap_to_open))
                        .setContentIntent(openPi)
                        .setVibrate(new long[]{0, 500, 200, 500})
                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);
        NotificationManager nm = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(prayerName.hashCode(), b.build());
    }

    public static void ensureNotificationChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    ctx.getString(com.islamiccompanion.app.R.string.notif_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(ctx.getString(
                    com.islamiccompanion.app.R.string.notif_channel_desc));
            channel.setSound(null, null);
            channel.enableVibration(false);
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    private PendingIntent buildOpenIntent(Context ctx) {
        Intent openIntent = new Intent(ctx, MainActivity.class);
        return PendingIntent.getActivity(ctx, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private String getNotifPrefKey(String prayerName) {
        switch (prayerName) {
            case "Fajr":    return AppPreferences.KEY_NOTIF_FAJR;
            case "Dhuhr":   return AppPreferences.KEY_NOTIF_DHUHR;
            case "Asr":     return AppPreferences.KEY_NOTIF_ASR;
            case "Maghrib": return AppPreferences.KEY_NOTIF_MAGHRIB;
            case "Isha":    return AppPreferences.KEY_NOTIF_ISHA;
            default:        return AppPreferences.KEY_NOTIF_FAJR;
        }
    }
}
