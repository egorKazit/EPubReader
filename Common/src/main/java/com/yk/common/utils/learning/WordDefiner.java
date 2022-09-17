package com.yk.common.utils.learning;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Word definer.
 * It uses external source to get definitions of word with parts of speech
 */
public class WordDefiner {

    private final static String URL = "https://wordsapiv1.p.rapidapi.com/words/%s/definitions";
    private final static String RAPID_API_HOST = "wordsapiv1.p.rapidapi.com";
    private final static String RAPID_API_KEY = "";

    /**
     * Method to get definitions
     *languages
     * @param wordToDefine word to define
     * @return definition
     * @throws WordOperatorException word operator exception
     */
    public WordDefinition getDefinitions(String wordToDefine) throws WordOperatorException {
        // prepare request
        Call call = new OkHttpClient().newCall(new Request.Builder().url(
                String.format(URL, wordToDefine))
                .header("X-RapidAPI-Host", RAPID_API_HOST)
                .header("X-RapidAPI-Key", RAPID_API_KEY)
                .get().build());
        Response response;
        try {
            // do call
            response = call.execute();
        } catch (IOException ioException) {
             throw new WordOperatorException("Error on definition", ioException);
        }
        // parse response
        String definitionsInJson = new BufferedReader(new InputStreamReader(response.body().byteStream()))
                .lines().collect(Collectors.joining());
        return new Gson().fromJson(definitionsInJson, WordDefinition.class);
    }

    /**
     * Word definition.
     * The class is used in GSON parsing
     */
    public static class WordDefinition {
        private String word;
        @Getter
        private List<SingleDefinition> definitions;
        @Getter
        private String message;
        @Getter
        private Boolean success;
    }

    /**
     * Single definition.
     * The class is used in GSON parsing
     */
    @Getter
    public static class SingleDefinition {
        private String definition;
        private String partOfSpeech;

    }

}
