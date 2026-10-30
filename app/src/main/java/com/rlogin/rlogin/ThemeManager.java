package com.rlogin.rlogin;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    public static final String PREFS_NAME = "rlogin_settings";
    public static final String KEY_THEME = "pref_app_theme";
    public static final String KEY_NIGHT_MODE = "pref_night_mode";

    public static final String THEME_DEFAULT = "default";
    public static final String THEME_BRUTALIST = "brutalist";
    public static final String THEME_CUSTOM = "custom";

    public static final int MODE_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    public static final int MODE_LIGHT = AppCompatDelegate.MODE_NIGHT_NO;
    public static final int MODE_DARK = AppCompatDelegate.MODE_NIGHT_YES;

    public static void applyTheme(Activity activity) {
        if (isBrutalist(activity)) {
            activity.setTheme(R.style.Theme_Rlogin_Brutalist);
        } else {
            activity.setTheme(R.style.Theme_Rlogin_Default);
        }
    }

    public static boolean isBrutalist(Context context) {
        String theme = getSelectedTheme(context);
        return THEME_BRUTALIST.equals(theme) || THEME_CUSTOM.equals(theme);
    }

    public static void applyNightMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int nightMode = prefs.getInt(KEY_NIGHT_MODE, MODE_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    public static String getSelectedTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.contains(KEY_THEME)) {
            return prefs.getString(KEY_THEME, THEME_DEFAULT);
        }
        // Fallback to legacy prefs file if present
        SharedPreferences legacy = context.getSharedPreferences("rlogin_theme_prefs", Context.MODE_PRIVATE);
        String legacyTheme = legacy.getString("selected_theme", null);
        if (legacyTheme != null) {
            prefs.edit().putString(KEY_THEME, legacyTheme).apply();
            return legacyTheme;
        }
        return THEME_DEFAULT;
    }

    public static void setSelectedTheme(Context context, String theme) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_THEME, theme).apply();
    }

    // Aliases for compatibility
    public static String getTheme(Context context) {
        return getSelectedTheme(context);
    }

    public static void setTheme(Context context, String theme) {
        setSelectedTheme(context, theme);
    }

    public static int getNightMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_NIGHT_MODE, MODE_SYSTEM);
    }

    public static void setNightMode(Context context, int mode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_NIGHT_MODE, mode).apply();
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}
