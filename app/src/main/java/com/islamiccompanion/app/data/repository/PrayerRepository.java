package com.islamiccompanion.app.data.repository;

import android.content.Context;

import com.islamiccompanion.app.data.prefs.AppPreferences;
import com.islamiccompanion.app.service.PrayerTimeCalculator;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/** Provides prayer-time data by delegating to {@link PrayerTimeCalculator}. */
public class PrayerRepository {

    private final AppPreferences prefs;

    public PrayerRepository(Context context) {
        prefs = new AppPreferences(context);
    }

    /**
     * Build a fresh {@link PrayerTimeCalculator} from the current user preferences.
     */
    public PrayerTimeCalculator buildCalculator() {
        double lat = prefs.getLastLatitude();
        double lng = prefs.getLastLongitude();

        Map<String, Integer> adj = new HashMap<>();
        adj.put("fajr",    prefs.getInt(AppPreferences.KEY_ADJ_FAJR,    0));
        adj.put("dhuhr",   prefs.getInt(AppPreferences.KEY_ADJ_DHUHR,   0));
        adj.put("asr",     prefs.getInt(AppPreferences.KEY_ADJ_ASR,     0));
        adj.put("maghrib", prefs.getInt(AppPreferences.KEY_ADJ_MAGHRIB, 0));
        adj.put("isha",    prefs.getInt(AppPreferences.KEY_ADJ_ISHA,    0));

        return new PrayerTimeCalculator(
                lat, lng,
                prefs.getCalculationMethod(),
                prefs.getMadhab(),
                adj
        );
    }

    /** Ordered map of prayer name → time for today. */
    public Map<String, Date> getTodayPrayerTimes() {
        return buildCalculator().getTodayPrayerMap();
    }

    public AppPreferences getPrefs() {
        return prefs;
    }
}
