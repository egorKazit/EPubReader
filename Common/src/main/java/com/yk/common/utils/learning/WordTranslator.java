package com.yk.common.utils.learning;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.yk.common.model.book.BookService;
import com.yk.common.model.book.BookServiceException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Word translator
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class WordTranslator {

    private final static String TRANSLATE_URL = "https://translation.googleapis.com/language/translate/v2?key=%s&q=%s&source=%s&target=%s";
    private final static String LANGUAGE_URL = "https://translation.googleapis.com/language/translate/v2/languages?key=%s&target=%s";
    private final static String KEY = "";

    private static List<Language> LANGUAGES;
    @Setter
    @Getter
    private static String language = "ru";

    /**
     * Method to get all available languages
     *
     * @return list of languages
     */
    public static List<Language> getLanguages() throws WordOperatorException {

        // if saved in buffer, then return from buffer
        if (LANGUAGES != null) {
            return LANGUAGES;
        }
        // Start new thread to do a http call
        Thread currentThread = Thread.currentThread();
        AtomicReference<WordOperatorException> wordOperatorExceptionAtomicReference = new AtomicReference<>();
        new Thread(() -> {
            try {
                // prepare a call
                String request = String.format(LANGUAGE_URL,
                        KEY,
                        Locale.getDefault().getLanguage());
                Call call = new OkHttpClient().newCall(new Request.Builder().url(request).get().build());
                // make it and handle response
                Response response = call.execute();
                String languagesInJson = new BufferedReader(new InputStreamReader(response.body().byteStream()))
                        .lines().collect(Collectors.joining());
                JSONArray languagesInJsonArray = new JSONObject(languagesInJson)
                        .getJSONObject("data").getJSONArray("languages");
                LANGUAGES = List.of(new Gson().fromJson(languagesInJsonArray.toString(), Language[].class));
            } catch (IOException | JSONException e) {
                // set exception if any
                wordOperatorExceptionAtomicReference.set(new WordOperatorException("Languages can not be retrieved", e));
            }
            synchronized (currentThread) {
                currentThread.notify();
            }
        }).start();
        try {
            // wait till call is finished
            synchronized (currentThread) {
                currentThread.wait(5_000);
            }
        } catch (InterruptedException e) {
            throw new WordOperatorException("Languages can not be retrieved", e);
        }
        // throw an exception if occurs in thread with http request
        if (wordOperatorExceptionAtomicReference.get() != null) {
            throw wordOperatorExceptionAtomicReference.get();
        }
        return LANGUAGES;
    }

    /**
     * Method to make a translation via external service
     *
     * @param originText origin text
     * @return translated text
     * @throws WordOperatorException exception on translation
     */
    public List<String> translateText(String originText) throws WordOperatorException {
        Response response;
        try {
            String request = String.format(TRANSLATE_URL,
                    KEY,
                    URLEncoder.encode(originText, StandardCharsets.UTF_8.name()),
                    BookService.getBookService().getLanguage(),
                    language);
            Call call = new OkHttpClient().newCall(new Request.Builder().url(request).get().build());
            response = call.execute();
        } catch (IOException | BookServiceException ioException) {
            throw new WordOperatorException("Error on translation: server is not reachable", ioException);
        }
        try {
            String translationInJson = new BufferedReader(new InputStreamReader(response.body().byteStream()))
                    .lines().collect(Collectors.joining());
            JSONArray jsonArray = new JSONObject(translationInJson)
                    .getJSONObject("data")
                    .getJSONArray("translations");
            List<String> translations = new ArrayList<>();
            for (int index = 0; index < jsonArray.length(); index++) {
                translations.add(jsonArray.getJSONObject(0)
                        .getString("translatedText"));
            }
            return translations;
        } catch (JSONException jsonException) {
            throw new WordOperatorException("Error on translation: invalid response", jsonException);
        }
    }

    @Getter
    public static class Language {
        private String language;
        private String name;
    }

}
