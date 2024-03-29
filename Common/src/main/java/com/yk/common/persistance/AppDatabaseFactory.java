package com.yk.common.persistance;

import android.content.Context;

import androidx.room.Room;

/**
 * Dao factory
 */

public final class AppDatabaseFactory {

    public static final String BOOKS = "books";

    public static AppDatabaseAbstract getFromContext(Context context) {
        return Room.databaseBuilder(context,
                AppDatabaseAbstract.class, BOOKS).build();
    }
}
