package com.yk.common.http;

import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.yk.common.context.ApplicationContext;
import com.yk.common.service.book.BookService;
import com.yk.common.service.book.BookServiceException;
import com.yk.common.service.dictionary.DictionaryService;
import com.yk.common.service.dictionary.LanguageService;

import java.util.Locale;

/**
 * Word speaker.
 * It allows to pronounce some words
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class WordSpeaker {

    private static TextToSpeech textToSpeech = null;

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
        new Thread(() -> {
            try {
                speakPhrase(DictionaryService.getInstance().getLastOriginWord(), BookService.getBookService().getLanguage());
            } catch (BookServiceException e) {
                Log.e("WordSpeaker", "Error on speech");
                Toast.makeText(ApplicationContext.getContext(), "Error on speech", Toast.LENGTH_LONG).show();
            }
        }).start();
    }

    /**
     * Method to speak target phrase.
     * The phrase is not coming in from outside, but is taken from dictionary pool
     */
    public static void speakTargetPhrase() {
        new Thread(() -> speakPhrase(DictionaryService.getMainTranslation(DictionaryService.getInstance().getLastTranslatedDictionary()),
                LanguageService.getInstance().getLanguage())).start();
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
