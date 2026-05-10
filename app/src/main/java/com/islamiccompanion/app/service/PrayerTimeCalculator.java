package com.islamiccompanion.app.service;

import com.batoulapps.adhan.CalculationMethod;
import com.batoulapps.adhan.CalculationParameters;
import com.batoulapps.adhan.Madhab;
import com.batoulapps.adhan.Prayer;
import com.batoulapps.adhan.PrayerTimes;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.data.DateComponents;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Wraps the Batoul Apps Adhan library to compute the five daily prayer times.
 *
 * TODO (RELIGIOUS ACCURACY): Cross-check output against IslamicFinder / Aladhan API
 *   for at least 5 cities across different time zones before release.
 */
public class PrayerTimeCalculator {

    /** Names of the supported calculation methods (matching the Adhan library enum). */
    public static final String[] METHOD_NAMES = {
            "MuslimWorldLeague", "Egyptian", "Karachi", "UmmAlQura",
            "NorthAmerica", "Dubai", "Kuwait", "Qatar", "Singapore",
            "MoonSightingCommittee", "Jafari"
    };

    private final double latitude;
    private final double longitude;
    private final String methodName;
    private final String madhabName;
    private final Map<String, Integer> adjustments;

    public PrayerTimeCalculator(
            double latitude,
            double longitude,
            String methodName,
            String madhabName,
            Map<String, Integer> adjustments) {
        this.latitude    = latitude;
        this.longitude   = longitude;
        this.methodName  = methodName;
        this.madhabName  = madhabName;
        this.adjustments = adjustments;
    }

    /**
     * Compute prayer times for today.
     *
     * @return {@link PrayerTimes} from the Adhan library.
     */
    public PrayerTimes getPrayerTimesForToday() {
        return getPrayerTimesForDate(new Date());
    }

    /**
     * Compute prayer times for a specific date.
     *
     * @param date the date to compute for.
     */
    public PrayerTimes getPrayerTimesForDate(Date date) {
        Coordinates coords = new Coordinates(latitude, longitude);
        DateComponents dateComponents = DateComponents.from(date);
        CalculationParameters params = resolveMethod().getParameters();

        // Apply madhab
        if ("hanafi".equalsIgnoreCase(madhabName)) {
            params.madhab = Madhab.HANAFI;
        } else {
            params.madhab = Madhab.SHAFI;
        }

        // Apply user-defined per-prayer adjustments (minutes)
        if (adjustments != null) {
            Integer adjFajr    = adjustments.get("fajr");
            Integer adjSunrise = adjustments.get("sunrise");
            Integer adjDhuhr   = adjustments.get("dhuhr");
            Integer adjAsr     = adjustments.get("asr");
            Integer adjMaghrib = adjustments.get("maghrib");
            Integer adjIsha    = adjustments.get("isha");
            // Minute-level offsets are applied via the offset map
            params.methodAdjustments.fajr    = adjFajr    != null ? adjFajr    : 0;
            params.methodAdjustments.sunrise = adjSunrise != null ? adjSunrise : 0;
            params.methodAdjustments.dhuhr   = adjDhuhr   != null ? adjDhuhr   : 0;
            params.methodAdjustments.asr     = adjAsr     != null ? adjAsr     : 0;
            params.methodAdjustments.maghrib = adjMaghrib != null ? adjMaghrib : 0;
            params.methodAdjustments.isha    = adjIsha    != null ? adjIsha    : 0;
        }

        return new PrayerTimes(coords, dateComponents, params);
    }

    /**
     * Return an ordered map of prayer name → Date for today.
     */
    public Map<String, Date> getTodayPrayerMap() {
        PrayerTimes times = getPrayerTimesForToday();
        Map<String, Date> map = new LinkedHashMap<>();
        map.put("Fajr",    times.fajr);
        map.put("Sunrise", times.sunrise);
        map.put("Dhuhr",   times.dhuhr);
        map.put("Asr",     times.asr);
        map.put("Maghrib", times.maghrib);
        map.put("Isha",    times.isha);
        return map;
    }

    /** Format a {@link Date} as "HH:mm" in the device's local timezone. */
    public static String formatTime(Date date) {
        if (date == null) return "--:--";
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date);
    }

    /** Which prayer is next after right now? Returns "Isha" if all have passed. */
    public String getNextPrayerName(PrayerTimes times) {
        Prayer next = times.nextPrayer();
        if (next == null) return "Isha";
        switch (next) {
            case FAJR:    return "Fajr";
            case SUNRISE: return "Sunrise";
            case DHUHR:   return "Dhuhr";
            case ASR:     return "Asr";
            case MAGHRIB: return "Maghrib";
            case ISHA:    return "Isha";
            default:      return "—";
        }
    }

    /** Time until the next prayer in milliseconds. */
    public long getMillisUntilNextPrayer(PrayerTimes times) {
        Date nextTime = times.timeForPrayer(times.nextPrayer());
        if (nextTime == null) return 0;
        long diff = nextTime.getTime() - System.currentTimeMillis();
        return Math.max(0, diff);
    }

    // ---- Private helpers ----

    private CalculationMethod resolveMethod() {
        switch (methodName) {
            case "Egyptian":              return CalculationMethod.EGYPTIAN;
            case "Karachi":               return CalculationMethod.KARACHI;
            case "UmmAlQura":             return CalculationMethod.UMM_AL_QURA;
            case "NorthAmerica":          return CalculationMethod.NORTH_AMERICA;
            case "Dubai":                 return CalculationMethod.DUBAI;
            case "Kuwait":                return CalculationMethod.KUWAIT;
            case "Qatar":                 return CalculationMethod.QATAR;
            case "Singapore":             return CalculationMethod.SINGAPORE;
            case "MoonSightingCommittee": return CalculationMethod.MOON_SIGHTING_COMMITTEE;
            case "Jafari":                return CalculationMethod.OTHER;
            case "MuslimWorldLeague":
            default:                      return CalculationMethod.MUSLIM_WORLD_LEAGUE;
        }
    }
}
