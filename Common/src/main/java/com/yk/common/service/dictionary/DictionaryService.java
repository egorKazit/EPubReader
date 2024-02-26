package com.yk.common.service.dictionary;

import androidx.annotation.NonNull;

import com.yk.common.R;
import com.yk.common.constants.GlobalConstants;
import com.yk.common.context.ApplicationContext;
import com.yk.common.http.WordOperatorException;
import com.yk.common.http.WordTranslator;
import com.yk.common.model.dictionary.Dictionary;
import com.yk.common.model.dictionary.WordTranslation;
import com.yk.common.service.book.BookService;
import com.yk.common.service.book.BookServiceException;

import org.jetbrains.annotations.Contract;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Dictionary service
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DictionaryService {

    private String lastOriginWord;
    private boolean isLastRequestSuccess;
    private Dictionary lastTranslatedDictionary;
    public final static String MAIN_TRANSLATION = "Main";

    /**
     * Method to get instance
     *
     * @return instance of class
     */
    public synchronized static DictionaryService getInstance() {
        return DictionaryServiceHolder.INSTANCE.dictionaryService;
    }

    /**
     * Initialization method.
     * It cleans up the last source/target translated word
     */
    public synchronized void init() {
        lastOriginWord = null;
        lastTranslatedDictionary = null;
        isLastRequestSuccess = false;
    }

    /**
     * Method to get list of all sorted alphabetically dictionaries
     *
     * @return list of all dictionaries
     */
    public List<Dictionary> getDictionaries() {
        return getDictionaries(true);
    }

    /**
     * Method to get list of all dictionaries
     *
     * @param isSortedAlphabetic is dictionaries need to be sorted alphabetically
     * @return list of all dictionaries
     */
    public List<Dictionary> getDictionaries(boolean isSortedAlphabetic) {
        var dictionaries = ApplicationContext.getContext()
                .getAppDatabaseAbstract()
                .dictionaryDao()
                .getDictionaries();
        return isSortedAlphabetic ? dictionaries.stream().sorted((firstDictionary, secondDictionary) ->
                        firstDictionary.getOriginWord().getOriginWord().compareToIgnoreCase(secondDictionary.getOriginWord().getOriginWord()))
                .collect(Collectors.toList()) : dictionaries;
    }

    /**
     * Method to search dictionaries based on provided pattern
     *
     * @param pattern pattern
     * @return dictionaries for the same pattern
     */
    public List<Dictionary> searchDictionaries(String pattern) {
        return ApplicationContext.getContext()
                .getAppDatabaseAbstract()
                .dictionaryDao().search(pattern)
                .stream().sorted((firstDictionary, secondDictionary) ->
                        firstDictionary.getOriginWord().getOriginWord().compareToIgnoreCase(secondDictionary.getOriginWord().getOriginWord()))
                .collect(Collectors.toList());
    }

    /**
     * Method to get dictionary by origin word
     *
     * @param originWord origin word
     * @return dictionary
     */
    @NonNull
    public Dictionary getDictionary(String originWord) {
        try {
            lastOriginWord = originWord;
            var dictionaryDao = ApplicationContext.getContext()
                    .getAppDatabaseAbstract()
                    .dictionaryDao();

            var sourceLanguage = BookService.getBookService().getLanguage();
            var targetLanguage = LanguageService.getInstance().getLanguage();
            // get dictionary from buffer
            var dictionary = dictionaryDao.getDictionaryByWord(originWord, sourceLanguage, targetLanguage);
            // if not presented then translate and get definition
            if (dictionary == null) {
                dictionary = WordTranslator.resolveTranslation(originWord, sourceLanguage, targetLanguage);
                // save to database
                dictionaryDao.addNewOriginWordWithTranslationsAndDefinitions(
                        dictionary.getOriginWord(),
                        dictionary.getTranslations(),
                        dictionary.getDefinitions() != null ? dictionary.getDefinitions() : List.of());
            }
            lastTranslatedDictionary = dictionary;
            isLastRequestSuccess = true;
            // return dictionary
            return dictionary;
        } catch (BookServiceException | WordOperatorException exception) {
            isLastRequestSuccess = false;
            return new Dictionary(null,
                    List.of(new WordTranslation(0, 0, MAIN_TRANSLATION, exception.getMessage())), null);
        }
    }

    /**
     * Method to get dictionary by origin word
     *
     * @param id dictionary id
     * @return dictionary
     */
    @NonNull
    public Dictionary getDictionary(int id) {
        return ApplicationContext.getContext()
                .getAppDatabaseAbstract()
                .dictionaryDao().getDictionary(id);
    }

    @NonNull
    @Contract(pure = true)
    public static String getMainTranslation(@NonNull Dictionary dictionary) {
        return dictionary.getTranslations().stream().filter(wordTranslation -> wordTranslation.getPartOfSpeech().equals(MAIN_TRANSLATION))
                .findFirst().orElseGet(() -> new WordTranslation(0, 0, MAIN_TRANSLATION, "")).getTranslation();
    }

    /**
     * Enum for lazy singleton
     */
    public enum DictionaryServiceHolder {
        INSTANCE();
        private final DictionaryService dictionaryService = new DictionaryService();
    }
}
