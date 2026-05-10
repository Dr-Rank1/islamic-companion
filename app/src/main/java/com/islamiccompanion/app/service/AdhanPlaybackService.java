package com.islamiccompanion.app.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import com.islamiccompanion.app.MainActivity;
import com.islamiccompanion.app.R;
import com.islamiccompanion.app.data.prefs.AppPreferences;

/**
 * Foreground service that plays the Adhan audio so it survives Doze mode.
 *
 * TODO (RELIGIOUS ACCURACY): Replace placeholder audio files in res/raw/ with
 *   verified, licensed Adhan recordings before publishing.
 *   - adhan_makkah.mp3 — Makkah Adhan (royalty-free / licensed)
 *   - adhan_madinah.mp3 — Madinah Adhan
 *   - adhan_aqsa.mp3 — Al-Aqsa Adhan
 *   - adhan_short_beep.mp3 — short notification beep fallback
 */
public class AdhanPlaybackService extends Service {

    public static final String ACTION_STOP = "com.islamiccompanion.app.ACTION_STOP_ADHAN";
    private static final int NOTIF_ID = 9001;

    private ExoPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();
        AdhanWorker.ensureNotificationChannel(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopPlayback();
            return START_NOT_STICKY;
        }

        String prayerName = (intent != null)
                ? intent.getStringExtra(AdhanWorker.EXTRA_PRAYER_NAME)
                : "Prayer";
        if (prayerName == null) prayerName = "Prayer";

        startForeground(NOTIF_ID, buildNotification(prayerName));
        playAdhan();
        return START_NOT_STICKY;
    }

    private void playAdhan() {
        AppPreferences prefs = new AppPreferences(this);
        String voice = prefs.getAdhanVoice();

        int rawResId = resolveRawResource(voice);
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + rawResId);

        if (player != null) {
            player.release();
        }
        player = new ExoPlayer.Builder(this).build();
        player.setMediaItem(MediaItem.fromUri(uri));
        player.addListener(new androidx.media3.common.Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == androidx.media3.common.Player.STATE_ENDED) {
                    stopPlayback();
                }
            }
        });
        player.prepare();
        player.play();
    }

    private int resolveRawResource(String voice) {
        switch (voice) {
            case "madinah": return R.raw.adhan_madinah;
            case "aqsa":    return R.raw.adhan_aqsa;
            case "beep":    return R.raw.adhan_short_beep;
            case "makkah":
            default:        return R.raw.adhan_makkah;
        }
    }

    private void stopPlayback() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        stopForeground(true);
        stopSelf();
    }

    private Notification buildNotification(String prayerName) {
        Intent stopIntent = new Intent(this, AdhanPlaybackService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPi = PendingIntent.getService(
                this, 0, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent openIntent = new Intent(this, MainActivity.class);
        PendingIntent openPi = PendingIntent.getActivity(
                this, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, AdhanWorker.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Time for " + prayerName)
                .setContentText("Tap to open app")
                .setContentIntent(openPi)
                .addAction(android.R.drawable.ic_media_pause, "Stop", stopPi)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.release();
            player = null;
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
