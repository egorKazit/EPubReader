package com.yk.common.service.dictionary;


import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.yk.common.context.ApplicationContext;
import com.yk.common.http.WordOperatorException;
import com.yk.common.http.WordTranslator;
import com.yk.common.model.dictionary.Language;
import com.yk.common.utils.Toaster;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public final class LanguageService {

    @Setter
    private String language = "ru";
    private List<Language> languages;

    /**
     * Method to get instance
     *
     * @return instance of class
     */
    public synchronized static LanguageService getInstance() {
        return LanguageServiceHolder.INSTANCE.languageService;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void init(Context context) {
        languages = new ArrayList<>();
        var languageDao = ApplicationContext.getContext()
                .getAppDatabaseAbstract()
                .languageDao();
        AtomicReference<WordOperatorException> wordOperatorExceptionAtomicReference = new AtomicReference<>();
        var futureLanguages = Executors.newSingleThreadExecutor().submit(() -> {
            var languagesLocal = languageDao.getAllLanguages();
            if (languagesLocal == null || languagesLocal.isEmpty()) {
                try {
                    languagesLocal = new ArrayList<>(WordTranslator.getLanguages());
                } catch (WordOperatorException exception) {
                    wordOperatorExceptionAtomicReference.set(exception);
                    return null;
                }
                languagesLocal.removeIf(languageEntry -> languageEntry.getName() == null);
                languageDao.addLanguages(languagesLocal.toArray(Language[]::new));
            }
            return languagesLocal;
        });
        try {
            var languages = futureLanguages.get();
            if (wordOperatorExceptionAtomicReference.get() != null) {
                Toaster.make(context, "Can not lead languages", wordOperatorExceptionAtomicReference.get());
                return;
            }
            this.languages.addAll(languages);
        } catch (ExecutionException | InterruptedException ignored) {
        }
    }

    /**
     * Enum for lazy singleton
     */
    private enum LanguageServiceHolder {
        INSTANCE();
        private final LanguageService languageService = new LanguageService();
    }

}
