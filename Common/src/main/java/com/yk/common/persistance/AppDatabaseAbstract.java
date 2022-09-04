package com.yk.common.persistance;

import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.yk.common.model.book.Book;
import com.yk.common.model.dictionary.OriginWord;
import com.yk.common.model.dictionary.WordDefinition;
import com.yk.common.model.dictionary.WordTranslation;

/**
 * DAO Abstract class
 */
@RequiresApi(api = Build.VERSION_CODES.S)
@Database(entities = {Book.class, OriginWord.class, WordTranslation.class, WordDefinition.class}, version = 2,
        autoMigrations = {
                @AutoMigration(from = 1, to = 2)
        }
)
@TypeConverters({DateConverter.class})
public abstract class AppDatabaseAbstract extends RoomDatabase {

    /**
     * Method to get DAO
     *
     * @return dao
     */
    public abstract BookDao bookDao();

    /**
     * Method to get dictionary DAO
     *
     * @return dictionary DAO
     */
    public abstract DictionaryDao dictionaryDao();

}
