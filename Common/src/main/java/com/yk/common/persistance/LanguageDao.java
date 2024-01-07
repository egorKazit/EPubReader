package com.yk.common.persistance;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.yk.common.model.dictionary.Language;

import java.util.List;

/**
 * Language Database access
 */
@Dao
public interface LanguageDao {

    /**
     * Method to select all books
     *
     * @return list of books
     */
    @Query("SELECT * FROM languages")
    List<Language> getAllLanguages();

    /**
     * Method to add new book
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addLanguages(Language... language);

}
