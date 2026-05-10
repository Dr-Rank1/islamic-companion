package com.islamiccompanion.app.util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Hijri (Islamic) date conversion utilities.
 *
 * Uses the Umm al-Qura algorithmic approach for accuracy.
 * The Julian Day Number (JDN) method is used for Gregorian↔Hijri conversion.
 */
public final class HijriDateUtil {

    private HijriDateUtil() {}

    /** Arabic month names. */
    private static final String[] HIJRI_MONTHS_EN = {
            "Muharram", "Safar", "Rabi al-Awwal", "Rabi al-Thani",
            "Jumada al-Ula", "Jumada al-Akhira", "Rajab", "Shaban",
            "Ramadan", "Shawwal", "Dhul-Qadah", "Dhul-Hijjah"
    };

    private static final String[] HIJRI_MONTHS_AR = {
            "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
            "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
            "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    };

    /** Notable Islamic dates: key = "hijriMonth-hijriDay", value = event name. */
    public static final Map<String, String> ISLAMIC_EVENTS = new HashMap<>();

    static {
        ISLAMIC_EVENTS.put("1-1",   "Islamic New Year");
        ISLAMIC_EVENTS.put("1-10",  "Day of Ashura");
        ISLAMIC_EVENTS.put("3-12",  "Mawlid al-Nabi");
        ISLAMIC_EVENTS.put("7-27",  "Isra and Mi'raj");
        ISLAMIC_EVENTS.put("8-15",  "Laylat al-Bara'at");
        ISLAMIC_EVENTS.put("9-1",   "First Day of Ramadan");
        ISLAMIC_EVENTS.put("9-27",  "Laylat al-Qadr");
        ISLAMIC_EVENTS.put("10-1",  "Eid al-Fitr");
        ISLAMIC_EVENTS.put("12-10", "Eid al-Adha");
    }

    /**
     * Convert a Gregorian date to Hijri using the JDN algorithm.
     *
     * @param year  Gregorian year
     * @param month Gregorian month (1–12)
     * @param day   Gregorian day (1–31)
     * @param offsetDays additional +/- days to apply (user preference for moon sighting)
     * @return int[3] = { hijriYear, hijriMonth (1–12), hijriDay }
     */
    public static int[] gregorianToHijri(int year, int month, int day, int offsetDays) {
        // Convert Gregorian to Julian Day Number
        int jdn = gregorianToJDN(year, month, day) + offsetDays;
        return jdnToHijri(jdn);
    }

    /** Return today's Hijri date as { year, month, day }. */
    public static int[] todayHijri(int offsetDays) {
        Calendar cal = Calendar.getInstance();
        return gregorianToHijri(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH),
                offsetDays
        );
    }

    /**
     * Return a human-readable Hijri date string, e.g. "15 Ramadan 1447".
     */
    public static String formatHijri(int[] hijri) {
        if (hijri == null || hijri.length < 3) return "";
        int month = hijri[1];
        String monthName = (month >= 1 && month <= 12)
                ? HIJRI_MONTHS_EN[month - 1] : "?";
        return hijri[2] + " " + monthName + " " + hijri[0];
    }

    public static String getMonthNameEn(int hijriMonth) {
        if (hijriMonth >= 1 && hijriMonth <= 12) return HIJRI_MONTHS_EN[hijriMonth - 1];
        return "";
    }

    public static String getMonthNameAr(int hijriMonth) {
        if (hijriMonth >= 1 && hijriMonth <= 12) return HIJRI_MONTHS_AR[hijriMonth - 1];
        return "";
    }

    /** Check if a given Hijri date has a notable Islamic event. */
    public static String getIslamicEvent(int hijriMonth, int hijriDay) {
        return ISLAMIC_EVENTS.get(hijriMonth + "-" + hijriDay);
    }

    // ---- Internal conversion helpers ----

    private static int gregorianToJDN(int y, int m, int d) {
        int a = (14 - m) / 12;
        int yr = y + 4800 - a;
        int mo = m + 12 * a - 3;
        return d + (153 * mo + 2) / 5 + 365 * yr + yr / 4 - yr / 100 + yr / 400 - 32045;
    }

    private static int[] jdnToHijri(int jdn) {
        int l = jdn - 1948440 + 10632;
        int n = (l - 1) / 10631;
        l = l - 10631 * n + 354;
        int j = ((10985 - l) / 5316) * ((50 * l) / 17719)
                + (l / 5670) * ((43 * l) / 15238);
        l = l - ((30 - j) / 15) * ((17719 * j) / 50)
                - (j / 16) * ((15238 * j) / 43) + 29;
        int month = (24 * l) / 709;
        int day   = l - (709 * month) / 24;
        int year  = 30 * n + j - 30;
        return new int[]{ year, month, day };
    }
}
