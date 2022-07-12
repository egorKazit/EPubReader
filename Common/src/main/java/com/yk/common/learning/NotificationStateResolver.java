package com.yk.common.learning;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.yk.common.utils.ApplicationContext;

/**
 * Class to store notification id
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class NotificationStateResolver {

    private static final String LAST_NOTIFICATION_FILE = "lastNotification.txt";
    private static final String LAST_NOTIFICATION_ID = "LAST_NOTIFICATION_ID";

    /**
     * Method to save notification id
     *
     * @param id notification id
     */
    @SuppressLint("ApplySharedPref")
    static void saveState(int id) {
        SharedPreferences sharedPreferences = ApplicationContext.getContext().getSharedPreferences(LAST_NOTIFICATION_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(LAST_NOTIFICATION_ID, id);
        editor.commit();
    }

    /**
     * method to release state
     */
    @SuppressLint("ApplySharedPref")
    public static void releaseState(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(LAST_NOTIFICATION_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(LAST_NOTIFICATION_ID);
        editor.commit();
    }

    /**
     * Method to check if notification exists
     *
     * @return true if notification exists
     */
    public static boolean hasActiveNotification(Context context) {
        return context.getSharedPreferences(LAST_NOTIFICATION_FILE, Context.MODE_PRIVATE).contains(LAST_NOTIFICATION_ID);
    }

}
