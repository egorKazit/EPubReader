package com.yk.common.persistance;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.yk.common.model.dictionary.Dictionary;
import com.yk.common.model.dictionary.OriginWord;
import com.yk.common.model.dictionary.WordDefinition;
import com.yk.common.model.dictionary.WordTranslation;

import java.util.List;

/**
 * Dictionary database access
 */

@Dao
public abstract class DictionaryDao {

    /**
     * Method to select all dictionaries
     *
     * @return list of dictionaries
     */
    @Transaction
    @Query("SELECT * FROM origin_word")
    public abstract List<Dictionary> getDictionaries();

    /**
     * Method to select all dictionaries
     *
     * @return list of dictionaries
     */
    @Transaction
    @Query("SELECT * FROM origin_word WHERE id = :id LIMIT 1")
    public abstract Dictionary getDictionary(int id);

    /**
     * Method to select a dictionary by origin word, source language and target language
     *
     * @return dictionary
     */
    @Transaction
    @Query("SELECT * FROM origin_word WHERE origin_word = :originWord AND source_language = :sourceLanguage AND target_language = :targetLanguage")
    public abstract Dictionary getDictionaryByWord(String originWord, String sourceLanguage, String targetLanguage);

    @Transaction
    @Query("SELECT DISTINCT wrd.* FROM  origin_word AS wrd LEFT JOIN word_translation trn ON wrd.id = trn.origin_word_id " +
            "WHERE wrd.origin_word LIKE :pattern OR trn.translation LIKE :pattern")

    public abstract List<Dictionary> search(String pattern);

    /**
     * Method to add new dictionary with translations and definitions
     *
     * @param originWord       origin word
     * @param wordTranslations translations
     * @param wordDefinitions  definitions
     */
    public void addNewOriginWordWithTranslationsAndDefinitions(OriginWord originWord,
                                                               List<WordTranslation> wordTranslations,
                                                               List<WordDefinition> wordDefinitions) {
        // add new origin word
        addNewOriginWord(originWord);
        // get saved origin word to retrieve id
        Dictionary savedDictionary = getDictionaryByWord(originWord.getOriginWord(), originWord.getSourceLanguage(), originWord.getTargetLanguage());
        // set parent id and save for translations and definitions
        wordTranslations.forEach(wordTranslation -> wordTranslation.setOriginWordId(savedDictionary.getOriginWord().getId()));
        addWordTranslations(wordTranslations);
        wordDefinitions.forEach(wordDefinition -> wordDefinition.setOriginWordId(savedDictionary.getOriginWord().getId()));
        addWordDefinitions(wordDefinitions);
    }

    /**
     * Method to add origin word
     */
    @Insert
    public abstract void addNewOriginWord(OriginWord originWord);

    /**
     * Method to add translations
     */
    @Insert
    public abstract void addWordTranslations(List<WordTranslation> wordTranslations);

    /**
     * Method to add definitions
     */
    @Insert
    public abstract void addWordDefinitions(List<WordDefinition> wordDefinitions);

}
