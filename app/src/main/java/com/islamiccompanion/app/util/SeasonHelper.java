package com.islamiccompanion.app.util;

import com.islamiccompanion.app.data.prefs.AppPreferences;

/**
 * Detects the current Islamic season from the Hijri date and returns a theme descriptor.
 * Used to automatically activate Ramadan and Eid seasonal themes.
 */
public final class SeasonHelper {

    public static final int SEASON_NORMAL   = 0;
    public static final int SEASON_RAMADAN  = 1;
    public static final int SEASON_EID_FITR = 2;
    public static final int SEASON_EID_ADHA = 3;

    private SeasonHelper() {}

    /**
     * Returns the current Islamic season based on the Hijri date.
     *
     * @param hijri int[3] = { year, month (1-12), day }
     */
    public static int getSeason(int[] hijri, AppPreferences prefs) {
        if (!prefs.getBoolean(AppPreferences.KEY_SEASONAL_THEMES, true)) return SEASON_NORMAL;
        if (hijri == null || hijri.length < 3) return SEASON_NORMAL;

        int month = hijri[1];
        int day   = hijri[2];

        if (month == 9) return SEASON_RAMADAN;
        if (month == 10 && day >= 1 && day <= 3) return SEASON_EID_FITR;
        if (month == 12 && day >= 10 && day <= 13) return SEASON_EID_ADHA;
        return SEASON_NORMAL;
    }

    /** Map season to a theme style name for dynamic theming. */
    public static String getThemeSuffix(int season) {
        switch (season) {
            case SEASON_RAMADAN:  return "Ramadan";
            case SEASON_EID_FITR:
            case SEASON_EID_ADHA: return "Eid";
            default:               return "";
        }
    }

    /** Whether Laylat al-Qadr window (last 10 nights of Ramadan). */
    public static boolean isLaylatAlQadrWindow(int[] hijri) {
        if (hijri == null || hijri.length < 3) return false;
        return hijri[1] == 9 && hijri[2] >= 21;
    }
}
