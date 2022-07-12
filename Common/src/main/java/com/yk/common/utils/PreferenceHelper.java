package com.yk.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.RequiresApi;

/**
 * Preference helper.
 * It allows to get/set all preference in application
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class PreferenceHelper {

    private final static String LEARNING_ENABLED = "LEARNING_ENABLED";
    private final static String LEARNING_INTERVAL = "LEARNING_INTERVAL";
    private final static String PREFERENCE_FILE = "PREFERENCE_FILE";

    private final SharedPreferences sharedPreferences;

    /**
     * Default constructor
     */
    public PreferenceHelper() {
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

}
