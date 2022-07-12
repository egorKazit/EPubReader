package com.yk.common.model.dictionary;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.yk.common.model.book.BookService;
import com.yk.common.model.book.BookServiceException;
import com.yk.common.utils.ApplicationContext;
import com.yk.common.utils.learning.WordDefiner;
import com.yk.common.utils.learning.WordOperatorException;
import com.yk.common.utils.learning.WordTranslator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Dictionary service
 */
@RequiresApi(api = Build.VERSION_CODES.S)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DictionaryService {

    /**
     * Method to get instance
     *
     * @return instance of class
     */
    public static DictionaryService getInstance() {
        return DictionaryServiceHolder.INSTANCE.dictionaryService;
    }

    /**
     * Method to get list of all dictionaries
     *
     * @return list of all dictionaries
     */
    List<Dictionary> getDictionaries() {
        return ApplicationContext.getContext()
                .getAppDatabaseAbstract()
                .dictionaryDao()
                .getDictionaries();
    }

    /**
     * Method to get dictionary by origin word
     *
     * @param originWord origin word
     * @return dictionary
     * @throws DictionaryServiceException exception on processing
     */
    Dictionary getDictionary(String originWord) throws DictionaryServiceException {
        try {
            // get dictionary from buffer
            Dictionary dictionary = ApplicationContext.getContext()
                    .getAppDatabaseAbstract()
                    .dictionaryDao()
                    .getDictionaryByWord(originWord, BookService.getBookService().getLanguage(), WordTranslator.getLanguage());
            // if not presented then translate and get definition
            if (dictionary == null) {
                dictionary = Dictionary.builder()
                        .originWord(
                                OriginWord.builder()
                                        .originWord(originWord)
                                        .sourceLanguage(BookService.getBookService().getLanguage())
                                        .targetLanguage(WordTranslator.getLanguage()).build())
                        .translations(new WordTranslator().translateText(originWord)
                                .stream().map(translation -> WordTranslation.builder().translation(translation).build())
                                .collect(Collectors.toList()))
                        .definitions(Optional.ofNullable(new WordDefiner().getDefinitions(originWord).getDefinitions())
                                .orElseGet(ArrayList::new)
                                .stream().map(singleDefinition -> WordDefinition.builder()
                                        .definition(singleDefinition.getDefinition())
                                        .partOfSpeech(singleDefinition.getPartOfSpeech()).build())
                                .collect(Collectors.toList())).build();
                // save to database
                ApplicationContext.getContext()
                        .getAppDatabaseAbstract()
                        .dictionaryDao()
                        .addNewOriginWordWithTranslationsAndDefinitions(
                                dictionary.getOriginWord(),
                                dictionary.getTranslations(),
                                dictionary.getDefinitions());
            } else if (dictionary.getTranslations() == null || dictionary.getTranslations().isEmpty()) {
                // if it's presented, but no definitions, then process definitions
                dictionary = Dictionary.builder()
                        .originWord(
                                OriginWord.builder()
                                        .originWord(originWord)
                                        .sourceLanguage(BookService.getBookService().getLanguage())
                                        .targetLanguage(WordTranslator.getLanguage()).build())
                        .translations(new WordTranslator().translateText(originWord)
                                .stream().map(s -> WordTranslation.builder()
                                        .translation(s).build())
                                .collect(Collectors.toList()))
                        .definitions(new WordDefiner().getDefinitions(originWord).getDefinitions()
                                .stream().map(singleDefinition -> WordDefinition.builder()
                                        .partOfSpeech(singleDefinition.getPartOfSpeech())
                                        .definition(singleDefinition.getDefinition())
                                        .build())
                                .collect(Collectors.toList()))
                        .build();
                // update database
                ApplicationContext.getContext()
                        .getAppDatabaseAbstract()
                        .dictionaryDao()
                        .updateTranslations(dictionary.getOriginWord(), dictionary.getTranslations())
                        .updateDefinitions(dictionary.getOriginWord(), dictionary.getDefinitions());
            }
            // return dictionary
            return dictionary;
        } catch (BookServiceException | WordOperatorException exception) {
            throw new DictionaryServiceException("Error on dictionary Operation", exception);
        }
    }

    /**
     * Enum for lazy singleton
     */
    private enum DictionaryServiceHolder {
        INSTANCE();
        private final DictionaryService dictionaryService = new DictionaryService();
    }
}
