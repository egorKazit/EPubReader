package com.yk.common.http;

import android.content.Context;

import com.yk.common.R;
import com.yk.common.context.ApplicationContext;
import com.yk.common.model.dictionary.Dictionary;
import com.yk.common.model.dictionary.Language;
import com.yk.common.model.dictionary.OriginWord;
import com.yk.common.model.dictionary.WordDefinition;
import com.yk.common.model.dictionary.WordTranslation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Word translator
 */
@SuppressWarnings("SpellCheckingInspection")
public abstract class WordTranslator {

    private static final String TRANSLATION_HOST = "https://translator.yk-general-one.cyou";
    public static final String LANGUAGES_PATH = "/languages";
    public static final String TRANSLATE_PATH = "/translate/%s-%s/%s";
    public static final String USERNAME = "requestor";
    public static final String PASSWORD = "1Word2LearnALot";

    /**
     * Method to get all available languages
     *
     * @return list of languages
     */
    public static List<Language> getLanguages(Context context) throws WordOperatorException {
        // Start new thread to do a http call
        try {
            return Arrays.asList(HttpCaller.get(TRANSLATION_HOST + LANGUAGES_PATH, USERNAME, PASSWORD, Language[].class));
        } catch (IOException e) {
            // throw exception if any
            throw new WordOperatorException(context.getString(R.string.no_lang_retrieved), e);
        }
    }

    /**
     * Method to make a translation via external service
     *
     * @param originText origin text
     * @return translated text
     * @throws WordOperatorException exception on translation
     */
    public static Dictionary resolveTranslation(String originText, String sourceLanguage, String targetLanguage) throws WordOperatorException {
        try {
            var url = String.format(TRANSLATION_HOST + TRANSLATE_PATH,
                    sourceLanguage, targetLanguage, originText);
            var dictionaryFromServer = HttpCaller.get(url, USERNAME, PASSWORD, DictionaryFromServer.class);
            if (dictionaryFromServer.translations == null)
                dictionaryFromServer.translations = new ArrayList<>();
            dictionaryFromServer.translations.add(new WordTranslation(0, 0, "Main", dictionaryFromServer.translation));
            return new Dictionary(new OriginWord(0, originText, sourceLanguage, targetLanguage),
                    dictionaryFromServer.getTranslations(),
                    dictionaryFromServer.getDefinitions());
        } catch (IOException exception) {
            // throw exception if any
            throw new WordOperatorException(ApplicationContext.getContext().getString(R.string.server_not_reachable), exception);
        }
    }

    @AllArgsConstructor
    @Getter
    public static class DictionaryFromServer {
        private String translation;
        private List<WordTranslation> translations;
        private List<WordDefinition> definitions;
    }

}
