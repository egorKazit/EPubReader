package com.yk.common.http;

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

    /**
     * Method to get all available languages
     *
     * @return list of languages
     */
    public static List<Language> getLanguages() throws WordOperatorException {
        // Start new thread to do a http call
        try {
            return Arrays.asList(HttpCaller.get("http://54.93.100.178:3000/languages", "requestor", "1Word2LearnALot", Language[].class));
        } catch (IOException e) {
            // throw exception if any
            throw new WordOperatorException("Languages can not be retrieved", e);
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
            var url = String.format("http://54.93.100.178:3000/translate/%s-%s/%s",
                    sourceLanguage, targetLanguage, originText);
            var dictionaryFromServer = HttpCaller.get(url, "requestor", "1Word2LearnALot", DictionaryFromServer.class);
            if (dictionaryFromServer.translations == null)
                dictionaryFromServer.translations = new ArrayList<>();
            dictionaryFromServer.translations.add(new WordTranslation(0, 0, "Main", dictionaryFromServer.translation));
            return new Dictionary(new OriginWord(0, originText, sourceLanguage, targetLanguage),
                    dictionaryFromServer.getTranslations(),
                    dictionaryFromServer.getDefinitions());
        } catch (IOException exception) {
            // throw exception if any
            throw new WordOperatorException("Error on translation: server is not reachable", exception);
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
