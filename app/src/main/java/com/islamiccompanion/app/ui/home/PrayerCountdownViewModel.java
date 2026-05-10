package com.islamiccompanion.app.ui.home;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.batoulapps.adhan.PrayerTimes;
import com.islamiccompanion.app.data.prefs.AppPreferences;
import com.islamiccompanion.app.data.repository.PrayerRepository;
import com.islamiccompanion.app.service.PrayerTimeCalculator;

import java.util.Locale;
import java.util.concurrent.Executors;

/**
 * Activity-scoped ViewModel that drives the persistent "Now" prayer countdown pill.
 * Lightweight — only computes the next prayer name and time-remaining countdown.
 * Shared between MainActivity (for the pill) and HomeFragment (reuses data).
 */
public class PrayerCountdownViewModel extends AndroidViewModel {

    private final PrayerRepository repo;
    private final MutableLiveData<String> countdownLive   = new MutableLiveData<>("00:00:00");
    private final MutableLiveData<String> nextPrayerLive  = new MutableLiveData<>("—");
    private final MutableLiveData<String> arabicNameLive  = new MutableLiveData<>("");

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable ticker;
    private PrayerTimes todayTimes;
    private PrayerTimeCalculator calc;

    private static final String[] ARABIC_NAMES =
            {"الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء"};
    private static final String[] EN_NAMES =
            {"Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha"};

    public PrayerCountdownViewModel(@NonNull Application application) {
        super(application);
        repo = new PrayerRepository(application);
        loadPrayerTimes();
    }

    private void loadPrayerTimes() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                calc       = repo.buildCalculator();
                todayTimes = calc.getPrayerTimesForToday();
                String next = calc.getNextPrayerName(todayTimes);
                nextPrayerLive.postValue(next);
                arabicNameLive.postValue(arabicFor(next));
            } catch (Exception ignored) {}
        });
    }

    /** Start ticking. Call from MainActivity.onStart(). */
    public void startTicking() {
        stopTicking();
        ticker = new Runnable() {
            @Override public void run() {
                if (todayTimes != null && calc != null) {
                    long ms = calc.getMillisUntilNextPrayer(todayTimes);
                    long hh = ms / 3_600_000;
                    long mm = (ms % 3_600_000) / 60_000;
                    long ss = (ms % 60_000) / 1_000;
                    countdownLive.setValue(
                            String.format(Locale.getDefault(), "%02d:%02d:%02d", hh, mm, ss));
                    // Refresh prayer names once per minute
                    if (ss == 0) {
                        String next = calc.getNextPrayerName(todayTimes);
                        nextPrayerLive.setValue(next);
                        arabicNameLive.setValue(arabicFor(next));
                    }
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(ticker);
    }

    /** Stop ticking. Call from MainActivity.onStop(). */
    public void stopTicking() {
        if (ticker != null) {
            handler.removeCallbacks(ticker);
            ticker = null;
        }
    }

    public LiveData<String> getCountdown()  { return countdownLive; }
    public LiveData<String> getNextPrayer() { return nextPrayerLive; }
    public LiveData<String> getArabicName() { return arabicNameLive; }

    private String arabicFor(String en) {
        for (int i = 0; i < EN_NAMES.length; i++) {
            if (EN_NAMES[i].equals(en)) return ARABIC_NAMES[i];
        }
        return "";
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopTicking();
    }
}
