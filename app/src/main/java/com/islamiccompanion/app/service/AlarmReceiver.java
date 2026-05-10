package com.islamiccompanion.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * Receives exact AlarmManager broadcasts for prayer times.
 * Acts as a redundant backup to WorkManager — fires even in Doze mode.
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String prayerName = intent.getStringExtra(com.islamiccompanion.app.util.AlarmHelper.EXTRA_PRAYER_NAME);
        if (prayerName == null) return;

        Log.d(TAG, "Exact alarm fired for " + prayerName);

        // Start the Adhan playback service
        Intent serviceIntent = new Intent(context, AdhanPlaybackService.class);
        serviceIntent.putExtra(AdhanWorker.EXTRA_PRAYER_NAME, prayerName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}
