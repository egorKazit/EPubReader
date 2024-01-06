package com.yk.common.service.dictionary;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

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
@RequiresApi(api = Build.VERSION_CODES.S)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DictionaryService {

    private String lastOriginWord;
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
    }

    /**
     * Method to get list of all dictionaries
     *
     * @return list of all dictionaries
     */
    public List<Dictionary> getDictionaries() {
        return ApplicationContext.getContext()
                .getAppDatabaseAbstract()
                .dictionaryDao()
                .getDictionaries().stream().sorted((firstDictionary, secondDictionary) ->
                        firstDictionary.getOriginWord().getOriginWord().compareToIgnoreCase(secondDictionary.getOriginWord().getOriginWord()))
                .collect(Collectors.toList());
    }

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
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public Dictionary getDictionary(String originWord) {
        try {
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
            lastOriginWord = originWord;
            lastTranslatedDictionary = dictionary;
            // return dictionary
            return dictionary;
        } catch (BookServiceException | WordOperatorException exception) {
            return new Dictionary(null,
                    List.of(new WordTranslation(0, 0, MAIN_TRANSLATION,
                            GlobalConstants.ERROR_ON_TRANSLATE + exception.getMessage())),
                    null);
        }
    }

    @NonNull
    @Contract(pure = true)
    @RequiresApi(api = Build.VERSION_CODES.S)
    public static String getMainTranslation(Dictionary dictionary) {
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
