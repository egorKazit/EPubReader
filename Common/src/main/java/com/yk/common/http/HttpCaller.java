package com.yk.common.http;

import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.yk.common.utils.ThreadOperator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class HttpCaller {

    static Gson gson = new Gson();

    public static <T> T get(String url, String username, String password, Class<T> targetType) throws IOException {
        AtomicReference<IOException> throwableAtomicReference = new AtomicReference<>();
        // call request synchronously
        var languagesInJson = ThreadOperator.getInstance(false).executeSingle(() -> {
            try {
                // set user/password
                String credential = Credentials.basic(username, password);
                Call call = new OkHttpClient().newCall(new Request.Builder().url(url).get().header(HttpHeaders.AUTHORIZATION, credential).build());
                // make it and handle response
                Response response = call.execute();
                return new BufferedReader(new InputStreamReader(response.body().byteStream()))
                        .lines().collect(Collectors.joining());
            } catch (IOException e) {
                throwableAtomicReference.set(e);
                return "";
            }
        }, () -> {
            throw new IOException("Error on loading");
        });
        if (throwableAtomicReference.get() != null)
            throw throwableAtomicReference.get();
        return gson.fromJson((String) languagesInJson, targetType);
    }
}