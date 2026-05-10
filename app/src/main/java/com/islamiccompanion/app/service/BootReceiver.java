package com.islamiccompanion.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Re-schedules prayer-time notifications when the device reboots, since
 * WorkManager queues are cleared on reboot.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            Log.d(TAG, "Device booted — rescheduling prayer times");
            AdhanWorker.scheduleTodaysPrayers(context);
        }
    }
}
