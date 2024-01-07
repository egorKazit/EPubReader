package com.yk.common.persistance;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.yk.common.model.book.Book;
import com.yk.common.model.dictionary.Language;
import com.yk.common.model.dictionary.OriginWord;
import com.yk.common.model.dictionary.WordDefinition;
import com.yk.common.model.dictionary.WordTranslation;

/**
 * DAO Abstract class
 */

@Database(entities = {Book.class, OriginWord.class, WordTranslation.class, WordDefinition.class, Language.class}, version = 1
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

    /**
     * Method to get dictionary DAO
     *
     * @return dictionary DAO
     */
    public abstract LanguageDao languageDao();

}
