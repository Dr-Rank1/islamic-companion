package com.islamiccompanion.app.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.islamiccompanion.app.data.db.AppDatabase;
import com.islamiccompanion.app.data.db.dao.PrayerLogDao;
import com.islamiccompanion.app.data.db.entity.PrayerLogEntry;
import com.islamiccompanion.app.data.prefs.AppPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Repository for prayer tracking, streaks, and analytics.
 * All data is stored locally — no cloud sync.
 */
public class StatsRepository {

    private final PrayerLogDao dao;
    private final AppPreferences prefs;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public StatsRepository(Context context) {
        this.dao = AppDatabase.getInstance(context).prayerLogDao();
        this.prefs = new AppPreferences(context);
    }

    /** Log a prayer with the given status. */
    public void logPrayer(String prayer, String status) {
        String today = todayString();
        executor.execute(() -> dao.log(new PrayerLogEntry(today, prayer, status)));
    }

    /** Whether the user has already logged a prayer for today. */
    public PrayerLogEntry getLogForToday(String prayer) {
        return dao.getEntry(todayString(), prayer);
    }

    /** LiveData of all entries since a given date (yyyy-MM-dd). */
    public LiveData<List<PrayerLogEntry>> getEntriesSince(String fromDate) {
        return dao.getEntriesSince(fromDate);
    }

    public LiveData<List<PrayerLogEntry>> getAllEntries() {
        return dao.getAllEntries();
    }

    /**
     * Compute the current prayer streak (consecutive days with at least one prayer logged
     * as "on_time" or "late").
     */
    public int computeCurrentStreak() {
        List<String> dates = dao.getDatesWithPrayerPrayed();
        if (dates.isEmpty()) return 0;

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        int streak = 0;

        for (String dateStr : dates) {
            String expected;
            try {
                expected = sdf.format(cal.getTime());
            } catch (Exception e) { break; }

            if (expected.equals(dateStr)) {
                streak++;
                cal.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                break;
            }
        }
        return streak;
    }

    public int computeBestStreak() {
        return prefs.getInt(AppPreferences.KEY_BEST_STREAK, 0);
    }

    public void updateBestStreak(int current) {
        int best = prefs.getInt(AppPreferences.KEY_BEST_STREAK, 0);
        if (current > best) {
            prefs.putInt(AppPreferences.KEY_BEST_STREAK, current);
        }
    }

    public int getTotalOnTime() { return dao.getTotalOnTime(); }
    public int getTotalLogged() { return dao.getTotalLogged(); }

    public void resetAllStats() {
        executor.execute(() -> {
            dao.deleteAll();
            prefs.putInt(AppPreferences.KEY_BEST_STREAK, 0);
        });
    }

    public String todayString() {
        return new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(new Date());
    }

    public String dateStringOffset(int daysBack) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -daysBack);
        return new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(cal.getTime());
    }
}
