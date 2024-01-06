package com.yk.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;

import com.yk.common.constants.ContentFont;
import com.yk.common.context.ApplicationContext;

import java.util.Arrays;

/**
 * Preference helper.
 * It allows to get/set all preference in application
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class PreferenceHelper {

    private final static String LEARNING_ENABLED = "LEARNING_ENABLED";
    private final static String LEARNING_INTERVAL = "LEARNING_INTERVAL";
    private final static String NIGHT_MODE = "NIGHT_MODE";
    private final static String CONTENT_FONT = "CONTENT_FONT";
    private final static String PREFERENCE_FILE = "PREFERENCE_FILE";

    private final SharedPreferences sharedPreferences;

    /**
     * Default constructor
     */
    private PreferenceHelper() {
        sharedPreferences = ApplicationContext.getContext().getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
    }


    /**
     * Method to get learning flag
     *
     * @return learning flag
     */
    public boolean isLearningEnabled() {
        return sharedPreferences.getBoolean(LEARNING_ENABLED, true);
    }

    /**
     * Method to get learning interval
     *
     * @return learning interval
     */
    public int getLearningInterval() {
        return sharedPreferences.getInt(LEARNING_INTERVAL, 20);
    }

    /**
     * Method to check if night mode is enabled
     *
     * @return true if night mode is enabled
     */
    public boolean isNightMode() {
        int nightMode = sharedPreferences.getInt(NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_NO);
        return nightMode == AppCompatDelegate.MODE_NIGHT_YES;
    }

    public ContentFont getContentFont() {
        String contentFontId = sharedPreferences.getString(CONTENT_FONT, ContentFont.DEFAULT.getId());
        return Arrays.stream(ContentFont.values())
                .filter(contentFont -> contentFont.getId().equals(contentFontId))
                .findFirst().orElse(ContentFont.DEFAULT);
    }

    /**
     * Method to set learning flag
     *
     * @param learningEnabled learning flag
     */
    public void setLearningEnabled(boolean learningEnabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(LEARNING_ENABLED, learningEnabled);
        editor.apply();
    }

    /**
     * Method to set learning interval
     *
     * @param learningInterval learning interval
     */
    public void setLearningInterval(int learningInterval) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(LEARNING_INTERVAL, learningInterval);
        editor.apply();
    }

    /**
     * Method to set night mode
     *
     * @param isNightMode night mode flag
     */
    public void enableNightMode(boolean isNightMode) {
        int nightMode;
        if (isNightMode) {
            nightMode = AppCompatDelegate.MODE_NIGHT_NO;
        } else {
            nightMode = AppCompatDelegate.MODE_NIGHT_YES;
        }
        AppCompatDelegate.setDefaultNightMode(nightMode);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(NIGHT_MODE, nightMode);
        editor.apply();
    }

    public void setContentFont(ContentFont contentFont) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CONTENT_FONT, contentFont.getId());
        editor.apply();
    }

    public enum PreferenceHelperHolder {
        INSTANCE;
        public final PreferenceHelper helper = new PreferenceHelper();
    }

}
