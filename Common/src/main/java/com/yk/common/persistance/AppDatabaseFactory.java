package com.yk.common.persistance;

import android.content.Context;

import androidx.room.Room;

/**
 * Dao factory
 */

public class AppDatabaseFactory {

    public static AppDatabaseAbstract getFromContext(Context context) {
        return Room.databaseBuilder(context,
                AppDatabaseAbstract.class, "books").build();
    }
}
