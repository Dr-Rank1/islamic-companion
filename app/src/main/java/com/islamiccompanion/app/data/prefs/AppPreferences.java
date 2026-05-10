package com.islamiccompanion.app.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.islamiccompanion.app.util.EncryptedPrefsHelper;

/**
 * Typed wrapper around EncryptedSharedPreferences for all user-configurable settings.
 * Backed by AES-256-GCM via the Android Keystore (v3+).
 * Falls back to regular SharedPreferences on devices where Keystore is unavailable.
 */
public class AppPreferences {

    // Kept for backwards-compat / migration source
    private static final String PREF_FILE = EncryptedPrefsHelper.ENCRYPTED_FILE;

    // Location
    public static final String KEY_LAST_LAT       = "last_lat";
    public static final String KEY_LAST_LNG       = "last_lng";
    public static final String KEY_LOCATION_MODE  = "location_mode"; // "auto" | "manual"
    public static final String KEY_MANUAL_CITY    = "manual_city";

    // Prayer calculation
    public static final String KEY_CALC_METHOD    = "calc_method";    // e.g. "MuslimWorldLeague"
    public static final String KEY_MADHAB         = "madhab";         // "shafi" | "hanafi"
    public static final String KEY_ADJ_FAJR       = "adj_fajr";
    public static final String KEY_ADJ_DHUHR      = "adj_dhuhr";
    public static final String KEY_ADJ_ASR        = "adj_asr";
    public static final String KEY_ADJ_MAGHRIB    = "adj_maghrib";
    public static final String KEY_ADJ_ISHA       = "adj_isha";
    public static final String KEY_HIJRI_OFFSET   = "hijri_offset";   // int, default 0

    // Adhan
    public static final String KEY_ADHAN_VOICE    = "adhan_voice";    // "makkah"|"madinah"|"aqsa"|"beep"
    public static final String KEY_NOTIF_FAJR     = "notif_fajr";     // "adhan"|"silent"|"vibrate"|"off"
    public static final String KEY_NOTIF_DHUHR    = "notif_dhuhr";
    public static final String KEY_NOTIF_ASR      = "notif_asr";
    public static final String KEY_NOTIF_MAGHRIB  = "notif_maghrib";
    public static final String KEY_NOTIF_ISHA     = "notif_isha";
    public static final String KEY_REMINDER_MINS  = "reminder_mins";  // 0 | 10 | 15 | 30

    // UI
    public static final String KEY_THEME          = "theme";          // "light"|"dark"|"system"
    public static final String KEY_LANGUAGE       = "language";       // "en"|"ar"|"id"|...

    // Quran
    public static final String KEY_QURAN_TEXT_SIZE  = "quran_text_size"; // "small"|"medium"|"large"|"xlarge"
    public static final String KEY_SHOW_TRANSLATION = "show_translation";
    public static final String KEY_RECITER          = "reciter";          // folder name on EveryAyah

    // First-launch flag
    public static final String KEY_FIRST_LAUNCH = "first_launch";

    // Ads
    public static final String KEY_ADS_LAST_INTERSTITIAL  = "ads_last_inter";   // timestamp ms
    public static final String KEY_ADS_REMOVED             = "ads_removed";      // lifetime IAP
    public static final String KEY_ADS_REMOVED_UNTIL       = "ads_removed_until";// rewarded 24h, epoch ms
    public static final String KEY_ADS_SESSION_COUNT       = "ads_session_count";// interstitials shown this session

    // IAP / billing
    public static final String KEY_IAP_PURCHASES           = "iap_purchases";    // JSON array of purchase tokens

    // App opens count (for rating prompt)
    public static final String KEY_APP_OPENS              = "app_opens";
    public static final String KEY_LAST_RATING_PROMPT     = "last_rating_prompt";
    public static final String KEY_HAS_RATED              = "has_rated";

    // Stats / streaks
    public static final String KEY_BEST_STREAK  = "best_streak";

    // Reliability
    public static final String KEY_HIGH_RELIABILITY = "high_reliability";

    // Seasonal themes
    public static final String KEY_SEASONAL_THEMES = "seasonal_themes";

    // Quran reading mode
    public static final String KEY_QURAN_PAGE_MODE = "quran_page_mode";

    private final SharedPreferences prefs;

    public AppPreferences(Context context) {
        // v3+: use EncryptedSharedPreferences backed by Android Keystore.
        prefs = EncryptedPrefsHelper.get(context.getApplicationContext());
    }

    // ----- Generic getters / setters -----

    public String getString(String key, String defVal) {
        return prefs.getString(key, defVal);
    }

    public void putString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    public int getInt(String key, int defVal) {
        return prefs.getInt(key, defVal);
    }

    public void putInt(String key, int value) {
        prefs.edit().putInt(key, value).apply();
    }

    public float getFloat(String key, float defVal) {
        return prefs.getFloat(key, defVal);
    }

    public void putFloat(String key, float value) {
        prefs.edit().putFloat(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defVal) {
        return prefs.getBoolean(key, defVal);
    }

    public void putBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
    }

    public long getLong(String key, long defVal) {
        return prefs.getLong(key, defVal);
    }

    public void putLong(String key, long value) {
        prefs.edit().putLong(key, value).apply();
    }

    // ----- Typed convenience methods -----

    public double getLastLatitude() {
        return (double) prefs.getFloat(KEY_LAST_LAT, 21.3891f); // default: Mecca
    }

    public double getLastLongitude() {
        return (double) prefs.getFloat(KEY_LAST_LNG, 39.8579f);
    }

    public void saveLastLocation(double lat, double lng) {
        prefs.edit()
             .putFloat(KEY_LAST_LAT, (float) lat)
             .putFloat(KEY_LAST_LNG, (float) lng)
             .apply();
    }

    public String getCalculationMethod() {
        return prefs.getString(KEY_CALC_METHOD, "MuslimWorldLeague");
    }

    public String getMadhab() {
        return prefs.getString(KEY_MADHAB, "shafi");
    }

    public int getHijriOffset() {
        return prefs.getInt(KEY_HIJRI_OFFSET, 0);
    }

    public String getAdhanVoice() {
        return prefs.getString(KEY_ADHAN_VOICE, "makkah");
    }

    public String getNotifMode(String prayerKey) {
        return prefs.getString(prayerKey, "adhan");
    }

    public int getReminderMinutes() {
        return prefs.getInt(KEY_REMINDER_MINS, 0);
    }

    public String getTheme() {
        return prefs.getString(KEY_THEME, "system");
    }

    public String getQuranTextSize() {
        return prefs.getString(KEY_QURAN_TEXT_SIZE, "large");
    }

    public boolean isShowTranslation() {
        return prefs.getBoolean(KEY_SHOW_TRANSLATION, true);
    }

    public String getReciter() {
        return prefs.getString(KEY_RECITER, "Alafasy_128kbps");
    }

    public boolean isFirstLaunch() {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    public void setFirstLaunchDone() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
    }
}
