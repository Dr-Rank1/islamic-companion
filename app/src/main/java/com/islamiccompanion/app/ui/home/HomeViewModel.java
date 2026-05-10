package com.islamiccompanion.app.ui.home;

import android.app.Application;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.islamiccompanion.app.data.prefs.AppPreferences;
import com.islamiccompanion.app.data.repository.HadithRepository;
import com.islamiccompanion.app.data.repository.PrayerRepository;
import com.islamiccompanion.app.data.repository.StatsRepository;
import com.islamiccompanion.app.model.Hadith;
import com.islamiccompanion.app.service.PrayerTimeCalculator;
import com.islamiccompanion.app.util.HijriDateUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

import com.batoulapps.adhan.PrayerTimes;

public class HomeViewModel extends AndroidViewModel {

    private final PrayerRepository prayerRepo;
    private final StatsRepository statsRepo;
    private final HadithRepository hadithRepo;
    private final AppPreferences prefs;

    private final MutableLiveData<Map<String, String>> prayerTimesLive = new MutableLiveData<>();
    private final MutableLiveData<String> nextPrayerNameLive = new MutableLiveData<>("—");
    private final MutableLiveData<String> nextPrayerTimeLive = new MutableLiveData<>("");
    private final MutableLiveData<String> countdownLive = new MutableLiveData<>("00:00:00");
    private final MutableLiveData<String> hijriDateLive = new MutableLiveData<>("");
    private final MutableLiveData<String> locationNameLive = new MutableLiveData<>("");
    private final MutableLiveData<String> hadithPreviewLive = new MutableLiveData<>("");
    private final MutableLiveData<Integer> streakCountLive = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> bestStreakLive = new MutableLiveData<>(0);
    private final MutableLiveData<String> prayerPromptLive = new MutableLiveData<>(null);

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable countdownRunnable;
    private PrayerTimes todayTimes;
    private PrayerTimeCalculator calc;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        prefs       = new AppPreferences(application);
        prayerRepo  = new PrayerRepository(application);
        statsRepo   = new StatsRepository(application);
        hadithRepo  = new HadithRepository(application);
        loadData();
        startCountdown();
    }

    public void refresh() {
        loadData();
    }

    private void loadData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                calc      = prayerRepo.buildCalculator();
                todayTimes = calc.getPrayerTimesForToday();

                // Prayer times map
                Map<String, String> times = new LinkedHashMap<>();
                for (Map.Entry<String, Date> e : prayerRepo.getTodayPrayerTimes().entrySet()) {
                    times.put(e.getKey(), PrayerTimeCalculator.formatTime(e.getValue()));
                }
                prayerTimesLive.postValue(times);

                // Next prayer
                String next = calc.getNextPrayerName(todayTimes);
                nextPrayerNameLive.postValue(next);
                Date nextTime = todayTimes.timeForPrayer(todayTimes.nextPrayer());
                if (nextTime != null) {
                    nextPrayerTimeLive.postValue(PrayerTimeCalculator.formatTime(nextTime));
                }

                // Hijri date
                int[] hijri = HijriDateUtil.todayHijri(prefs.getHijriOffset());
                hijriDateLive.postValue(HijriDateUtil.formatHijri(hijri));

                // Location name (reverse geocode) — best-effort
                String loc = prefs.getString(AppPreferences.KEY_MANUAL_CITY, "");
                if (!loc.isEmpty()) {
                    locationNameLive.postValue(loc.split(",")[0]);
                } else {
                    try {
                        double lat = prefs.getLastLatitude();
                        double lng = prefs.getLastLongitude();
                        Geocoder geo = new Geocoder(getApplication(), Locale.getDefault());
                        var addrs = geo.getFromLocation(lat, lng, 1);
                        if (addrs != null && !addrs.isEmpty()) {
                            String city = addrs.get(0).getLocality();
                            if (city == null) city = addrs.get(0).getAdminArea();
                            locationNameLive.postValue(city);
                        }
                    } catch (Exception ignored) {}
                }

                // Hadith preview
                Hadith hadith = hadithRepo.getHadithOfTheDay();
                if (hadith != null) {
                    hadithPreviewLive.postValue(hadith.getTextEn());
                }

                // Streak
                int streak = statsRepo.computeCurrentStreak();
                int best   = statsRepo.computeBestStreak();
                statsRepo.updateBestStreak(streak);
                streakCountLive.postValue(streak);
                bestStreakLive.postValue(best);

                // Prayer prompt — check if the last passed prayer is unlogged
                checkPrayerPrompt();

            } catch (Exception e) {
                // Non-fatal — UI shows defaults
            }
        });
    }

    private void checkPrayerPrompt() {
        if (todayTimes == null) return;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String[] prayers = {"Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"};
        Date[] times = {
                todayTimes.fajr, todayTimes.dhuhr, todayTimes.asr,
                todayTimes.maghrib, todayTimes.isha
        };
        long now = System.currentTimeMillis();
        for (int i = prayers.length - 1; i >= 0; i--) {
            if (times[i] != null && times[i].getTime() < now) {
                // This prayer has passed — check if it's logged
                if (statsRepo.getLogForToday(prayers[i]) == null) {
                    prayerPromptLive.postValue(prayers[i]);
                }
                return;
            }
        }
        prayerPromptLive.postValue(null);
    }

    public void logPrayer(String prayer, String status) {
        statsRepo.logPrayer(prayer, status);
        prayerPromptLive.postValue(null);
        // Refresh streak
        Executors.newSingleThreadExecutor().execute(() -> {
            int streak = statsRepo.computeCurrentStreak();
            statsRepo.updateBestStreak(streak);
            streakCountLive.postValue(streak);
            bestStreakLive.postValue(statsRepo.computeBestStreak());
        });
    }

    private void startCountdown() {
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                if (todayTimes != null && calc != null) {
                    long ms = calc.getMillisUntilNextPrayer(todayTimes);
                    long hh = ms / 3_600_000;
                    long mm = (ms % 3_600_000) / 60_000;
                    long ss = (ms % 60_000) / 1_000;
                    countdownLive.setValue(
                            String.format(Locale.getDefault(), "%02d:%02d:%02d", hh, mm, ss));
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(countdownRunnable);
    }

    public LiveData<Map<String, String>> getPrayerTimes() { return prayerTimesLive; }
    public LiveData<String> getNextPrayerName()     { return nextPrayerNameLive; }
    public LiveData<String> getNextPrayerTime()     { return nextPrayerTimeLive; }
    public LiveData<String> getCountdown()          { return countdownLive; }
    public LiveData<String> getHijriDate()          { return hijriDateLive; }
    public LiveData<String> getLocationName()       { return locationNameLive; }
    public LiveData<String> getHadithPreview()      { return hadithPreviewLive; }
    public LiveData<Integer> getStreakCount()        { return streakCountLive; }
    public LiveData<Integer> getBestStreak()         { return bestStreakLive; }
    public LiveData<String> getPrayerPrompt()        { return prayerPromptLive; }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (countdownRunnable != null) handler.removeCallbacks(countdownRunnable);
    }
}
