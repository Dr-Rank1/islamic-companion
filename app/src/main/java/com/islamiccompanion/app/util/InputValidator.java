package com.islamiccompanion.app.util;

/**
 * Input validation and sanitization utilities.
 * Used for all data arriving from non-trusted sources:
 * user input, deep links, network responses, file imports.
 */
public final class InputValidator {

    public static final int MAX_CITY_LENGTH    = 100;
    public static final int MAX_DHIKR_LENGTH   = 200;
    public static final int MAX_NOTE_LENGTH    = 500;
    public static final int MAX_SEARCH_LENGTH  = 150;

    /** Quran surah range (1–114). */
    public static final int SURAH_MIN = 1;
    public static final int SURAH_MAX = 114;

    private InputValidator() {}

    /**
     * Validate a surah number arriving from a deep link or external source.
     * Returns the sanitized value, or 1 on invalid input.
     */
    public static int validateSurah(String raw) {
        try {
            int n = Integer.parseInt(raw.trim());
            return (n >= SURAH_MIN && n <= SURAH_MAX) ? n : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    /**
     * Validate an ayah number for a given surah (1–ayahCount).
     * Returns 1 if out of range or invalid.
     */
    public static int validateAyah(String raw, int maxAyahForSurah) {
        try {
            int n = Integer.parseInt(raw.trim());
            return (n >= 1 && n <= maxAyahForSurah) ? n : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    /**
     * Sanitize free-text user input:
     *  - Strip control characters (ASCII 0-31 except newline/tab)
     *  - Strip Unicode text-direction overrides (U+202A-U+202E, U+2066-U+2069)
     *  - Cap to maxLength chars
     */
    public static String sanitizeText(String input, int maxLength) {
        if (input == null) return "";
        String s = input
                // Strip control characters except newline/tab/carriage return
                .replaceAll("[\\p{Cc}&&[^\n\r\t]]", "")
                // Strip Unicode bidi override characters
                .replaceAll("[‪-‮⁦-⁩]", "")
                .trim();
        return s.length() <= maxLength ? s : s.substring(0, maxLength);
    }

    /** Sanitize a city name for use in a query. */
    public static String sanitizeCity(String input) {
        return sanitizeText(input, MAX_CITY_LENGTH);
    }

    /** Sanitize a custom dhikr name. */
    public static String sanitizeDhikr(String input) {
        return sanitizeText(input, MAX_DHIKR_LENGTH);
    }

    /** Sanitize a bookmark note. */
    public static String sanitizeNote(String input) {
        return sanitizeText(input, MAX_NOTE_LENGTH);
    }

    /** Sanitize a search query. */
    public static String sanitizeSearch(String input) {
        return sanitizeText(input, MAX_SEARCH_LENGTH);
    }

    /**
     * Validate a prayer name arriving from a deep link or intent extra.
     * Returns the name if valid, or "Fajr" as a safe fallback.
     */
    public static String validatePrayerName(String raw) {
        if (raw == null) return "Fajr";
        switch (raw.trim()) {
            case "Fajr":    return "Fajr";
            case "Dhuhr":   return "Dhuhr";
            case "Asr":     return "Asr";
            case "Maghrib": return "Maghrib";
            case "Isha":    return "Isha";
            default:        return "Fajr";
        }
    }

    /** Clamp an integer to [min, max]. */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /** Check if a string looks like a valid URI host (basic check). */
    public static boolean isValidHost(String host) {
        if (host == null || host.isEmpty()) return false;
        return host.matches("[a-zA-Z0-9.-]+");
    }

    /** Cap a network response body at 5 MB before parsing. */
    public static final long MAX_RESPONSE_BODY_BYTES = 5 * 1024 * 1024L;
}
