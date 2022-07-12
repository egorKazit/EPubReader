package com.yk.common.persistance;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.room.Room;

/**
 * Dao factory
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class AppDatabaseFactory {

    public static AppDatabaseAbstract getFromContext(Context context) {
        return Room.databaseBuilder(context,
                AppDatabaseAbstract.class, "books").build();
    }
}
