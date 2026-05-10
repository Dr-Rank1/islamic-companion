package com.islamiccompanion.app.ui.stats;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.islamiccompanion.app.data.db.entity.PrayerLogEntry;
import com.islamiccompanion.app.data.repository.StatsRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class StatsViewModel extends AndroidViewModel {

    private final StatsRepository repo;

    private final MutableLiveData<Integer> currentStreakLive = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> bestStreakLive = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> onTimePctLive = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> totalLoggedLive = new MutableLiveData<>(0);
    private final MutableLiveData<String> hardestPrayerLive = new MutableLiveData<>("");
    private final MutableLiveData<int[]> weeklyDataLive = new MutableLiveData<>(new int[7]);

    public StatsViewModel(@NonNull Application application) {
        super(application);
        repo = new StatsRepository(application);
        load();
        observeEntries();
    }

    private void load() {
        Executors.newSingleThreadExecutor().execute(() -> {
            int streak  = repo.computeCurrentStreak();
            int best    = repo.computeBestStreak();
            int total   = repo.getTotalLogged();
            int onTime  = repo.getTotalOnTime();
            int pct     = total > 0 ? (int) ((onTime * 100f) / total) : 0;

            currentStreakLive.postValue(streak);
            bestStreakLive.postValue(best);
            totalLoggedLive.postValue(total);
            onTimePctLive.postValue(pct);
        });
    }

    private void observeEntries() {
        String fromDate = repo.dateStringOffset(7);
        repo.getEntriesSince(fromDate).observeForever(entries -> {
            if (entries == null) return;
            computeWeeklyData(entries);
            computeHardestPrayer(entries);
        });
    }

    private void computeWeeklyData(List<PrayerLogEntry> entries) {
        int[] counts = new int[7]; // index 0 = 6 days ago, 6 = today
        for (PrayerLogEntry e : entries) {
            if (PrayerLogEntry.MISSED.equals(e.getStatus())) continue;
            int daysAgo = daysAgoFrom(e.getDate());
            if (daysAgo >= 0 && daysAgo < 7) {
                counts[6 - daysAgo]++;
            }
        }
        weeklyDataLive.postValue(counts);
    }

    private void computeHardestPrayer(List<PrayerLogEntry> entries) {
        Map<String, Integer> missedCounts = new HashMap<>();
        for (PrayerLogEntry e : entries) {
            if (PrayerLogEntry.MISSED.equals(e.getStatus())) {
                missedCounts.merge(e.getPrayer(), 1, Integer::sum);
            }
        }
        String hardest = "";
        int max = 0;
        for (Map.Entry<String, Integer> en : missedCounts.entrySet()) {
            if (en.getValue() > max) { max = en.getValue(); hardest = en.getKey(); }
        }
        hardestPrayerLive.postValue(max > 0 ? hardest : "");
    }

    private int daysAgoFrom(String dateStr) {
        try {
            java.text.SimpleDateFormat sdf =
                    new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date d = sdf.parse(dateStr);
            long diff = System.currentTimeMillis() - (d != null ? d.getTime() : 0);
            return (int) (diff / 86_400_000L);
        } catch (Exception e) { return -1; }
    }

    public void resetAllStats() {
        repo.resetAllStats();
        currentStreakLive.setValue(0);
        bestStreakLive.setValue(0);
        onTimePctLive.setValue(0);
        totalLoggedLive.setValue(0);
        weeklyDataLive.setValue(new int[7]);
        hardestPrayerLive.setValue("");
    }

    public LiveData<Integer> getCurrentStreak()  { return currentStreakLive; }
    public LiveData<Integer> getBestStreak()      { return bestStreakLive; }
    public LiveData<Integer> getOnTimePct()       { return onTimePctLive; }
    public LiveData<Integer> getTotalLogged()     { return totalLoggedLive; }
    public LiveData<String> getHardestPrayer()   { return hardestPrayerLive; }
    public LiveData<int[]> getWeeklyData()        { return weeklyDataLive; }
}
