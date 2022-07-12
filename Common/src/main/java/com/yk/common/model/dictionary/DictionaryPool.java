package com.yk.common.model.dictionary;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.yk.common.constants.GlobalConstants;
import com.yk.common.model.book.BookService;
import com.yk.common.model.book.BookServiceException;
import com.yk.common.utils.learning.WordTranslator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import lombok.Getter;

/**
 * Dictionary pool.
 * It contains already loaded dictionary and updates it on adding or deleting.
 * Also it has the last source/target translated word
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class DictionaryPool {

    private static final List<Dictionary> DICTIONARIES = new ArrayList<>();
    private static boolean isLoaded = false;
    @Getter
    private static String lastOriginWord;
    @Getter
    private static String lastTranslatedWord;

    /**
     * Initialization method.
     * It cleans up the last source/target translated word
     */
    public synchronized static void init() {
        lastOriginWord = null;
        lastTranslatedWord = null;
    }

    /**
     * Method to get dictionaries.
     *
     * @return list of dictionaries
     */
    public synchronized static List<Dictionary> getDictionaries() {
        // start loading if not loaded
        if (!isLoaded) {
            Thread currentThread = Thread.currentThread();
            // initiate new thread to make a query to database via dictionary service
            new Thread(() -> {
                DICTIONARIES.clear();
                DICTIONARIES.addAll(DictionaryService.getInstance().getDictionaries());
                synchronized (currentThread) {
                    // notify main thread that data is loaded
                    currentThread.notify();
                }
            }).start();
            // wait in current thread for 5 seconds or till notification for "loading" thread
            synchronized (currentThread) {
                try {
                    currentThread.wait(5_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // update loading status
            isLoaded = true;
        }
        return DICTIONARIES;
    }

    /**
     * Method to translate a single word
     *
     * @param sourceText source text
     * @return translated text
     */
    public static String getWordTranslation(String sourceText) {
        // if sourceText is one word, then set it as last origin word
        if (!sourceText.contains(" "))
            lastOriginWord = sourceText;
        // go through dictionaries and check if the word is already presented
        Optional<Dictionary> dictionaryToReturn = DICTIONARIES
                .stream()
                .filter(dictionary -> {
                    try {
                        return dictionary.getOriginWord().getSourceLanguage().equals(BookService.getBookService().getLanguage()) &&
                                dictionary.getOriginWord().getTargetLanguage().equals(WordTranslator.getLanguage()) &&
                                dictionary.getOriginWord().getOriginWord().equals(sourceText);
                    } catch (BookServiceException bookServiceException) {
                        throw new RuntimeException("No Language " + bookServiceException.getMessage());
                    }
                }).findFirst();
        // use translation from buffer if presented
        if (dictionaryToReturn.isPresent()) {
            Optional<WordTranslation> wordTranslation = dictionaryToReturn.get().getTranslations().stream().findFirst();
            if (wordTranslation.isPresent()) {
                lastTranslatedWord = wordTranslation.get().getTranslation();
                return lastTranslatedWord;
            }
        }
        try {
            // translate via the dictionary service
            Dictionary dictionary = DictionaryService.getInstance().getDictionary(sourceText);
            if (dictionary.getTranslations().size() == 0) {
                lastTranslatedWord = null;
                return "The translation was not found";
            }
            // add to dictionary if source text is a single word
            if (!sourceText.contains(" "))
                DICTIONARIES.add(dictionary);
            lastTranslatedWord = dictionary.getTranslations().get(0).getTranslation();
            return lastTranslatedWord;
        } catch (DictionaryServiceException dictionaryServiceException) {
            return GlobalConstants.ERROR_ON_TRANSLATE + dictionaryServiceException.getMessage();
        }
    }

    /**
     * Method to get learning entry
     *
     * @return learning entry
     */
    public static LearningEntry getLearningEntry(Context context) {
        List<String> possibleTranslations = new ArrayList<>();
        int currentPosition = new LearningStateOperator().getWordPosition(context);
        if (currentPosition > 0) {
            int previousPosition = new Random().ints(0, currentPosition).findFirst().orElse(0);
            possibleTranslations.add(getDictionaries().get(previousPosition).getTranslations().get(0).getTranslation());
        }
        Dictionary currentDictionary = getDictionaries().get(currentPosition);
        possibleTranslations.add(currentDictionary.getTranslations().get(0).getTranslation());
        if ((currentPosition + 1) < getDictionaries().size()) {
            int nextPosition = new Random().ints(currentPosition + 1, getDictionaries().size()).findFirst().orElse(getDictionaries().size());
            possibleTranslations.add(getDictionaries().get(nextPosition).getTranslations().get(0).getTranslation());
        }
        Collections.shuffle(possibleTranslations, new Random());
        return LearningEntry.builder()
                .originWord(currentDictionary.getOriginWord().getOriginWord())
                .correctTranslation(currentDictionary.getTranslations().get(0).getTranslation())
                .possibleTranslations(possibleTranslations)
                .build();
    }

    /**
     * Method to notify about correct answer
     */
    public static void markCorrectLearning(Context context) {
        LearningStateOperator learningStateOperator = new LearningStateOperator();
        int currentPosition = learningStateOperator.getWordPosition(context);
        currentPosition++;
        if (currentPosition == getDictionaries().size())
            currentPosition = 0;
        learningStateOperator.setWordPosition(currentPosition,context);
    }
}