package com.islamiccompanion.app.util;

import androidx.appcompat.app.AppCompatDelegate;

/** Applies the user's chosen theme (light / dark / system). */
public final class ThemeHelper {

    private ThemeHelper() {}

    /**
     * Apply the theme based on the string preference stored by the user.
     *
     * @param theme "light" | "dark" | "system"
     */
    public static void applyTheme(String theme) {
        switch (theme) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}
