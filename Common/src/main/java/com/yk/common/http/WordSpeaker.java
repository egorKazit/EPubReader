package com.yk.common.http;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.yk.common.R;
import com.yk.common.context.ApplicationContext;
import com.yk.common.service.book.BookService;
import com.yk.common.service.book.BookServiceException;
import com.yk.common.service.dictionary.DictionaryService;
import com.yk.common.service.dictionary.LanguageService;

import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Word speaker.
 * It allows to pronounce some words
 */

public final class WordSpeaker {

    private static TextToSpeech textToSpeech = null;
    private static final ExecutorService speaker = Executors.newSingleThreadExecutor();

    /**
     * initialization of textToSpeech
     */
    public static void init() {
        if (textToSpeech != null)
            return;
        textToSpeech = new TextToSpeech(ApplicationContext.getContext(), status -> {
            if (status == TextToSpeech.ERROR) {
                throw new RuntimeException("");
            }
        });
    }

    /**
     * Method to speak source phrase.
     * The phrase is not coming in from outside, but is taken from dictionary pool
     */
    public static void speakSourcePhrase() {
        speaker.submit(() -> {
            try {
                speakPhrase(DictionaryService.getInstance().getLastOriginWord(), BookService.getBookService().getLanguage());
            } catch (BookServiceException e) {
                Log.e("WordSpeaker", "Error on speech");
                Toast.makeText(ApplicationContext.getContext(), ApplicationContext.getContext().getString(R.string.error_on_speech), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Method to speak target phrase.
     * The phrase is not coming in from outside, but is taken from dictionary pool
     */
    public static void speakTargetPhrase() {
        speaker.submit(() -> speakPhrase(DictionaryService.getMainTranslation(DictionaryService.getInstance().getLastTranslatedDictionary()),
                LanguageService.getInstance().getLanguage()));
    }

    /**
     * Method to speak any phrase.
     *
     * @param text     text to speak
     * @param language language of speaking
     */
    private static synchronized void speakPhrase(CharSequence text, String language) {
        assert textToSpeech != null;
        textToSpeech.setLanguage(Locale.forLanguageTag(language));
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, new Bundle(), "TranslationForAll");
    }

}
