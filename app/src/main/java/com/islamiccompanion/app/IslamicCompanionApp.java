package com.islamiccompanion.app;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.ads.MobileAds;
import com.islamiccompanion.app.util.AdHelper;
import com.islamiccompanion.app.data.prefs.AppPreferences;
import com.islamiccompanion.app.util.EncryptedPrefsHelper;
import com.islamiccompanion.app.util.HijriDateUtil;
import com.islamiccompanion.app.util.SeasonHelper;
import com.islamiccompanion.app.util.ThemeHelper;

import java.util.concurrent.Executors;

/** Application class — initialises singletons and detects the current Islamic season. */
public class IslamicCompanionApp extends Application {

    private static IslamicCompanionApp instance;

    private final MutableLiveData<Integer> currentSeason = new MutableLiveData<>(SeasonHelper.SEASON_NORMAL);

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // v3: migrate plaintext prefs → encrypted prefs (no-op if already done)
        EncryptedPrefsHelper.migrateFromPlaintext(this);

        AppPreferences prefs = new AppPreferences(this);
        ThemeHelper.applyTheme(prefs.getTheme());

        // Detect the current Islamic season on a background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            int[] hijri = HijriDateUtil.todayHijri(prefs.getHijriOffset());
            int season  = SeasonHelper.getSeason(hijri, prefs);
            currentSeason.postValue(season);
        });

        // Configure AdMob BEFORE initialize (sets PG content rating filter).
        // TODO: gate MobileAds.initialize() behind UMP consent for EEA/UK users.
        AdHelper.configure();
        MobileAds.initialize(this, status -> { /* ready */ });
    }

    public static IslamicCompanionApp get() { return instance; }

    /** Observable current Islamic season — see {@link SeasonHelper} constants. */
    public LiveData<Integer> getSeasonLive() { return currentSeason; }

    public int getSeason() {
        Integer s = currentSeason.getValue();
        return s != null ? s : SeasonHelper.SEASON_NORMAL;
    }
}
