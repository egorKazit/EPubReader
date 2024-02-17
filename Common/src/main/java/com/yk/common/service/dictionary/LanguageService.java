package com.yk.common.service.dictionary;


import android.content.Context;

import com.yk.common.R;
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
                    languagesLocal = new ArrayList<>(WordTranslator.getLanguages(context));
                } catch (WordOperatorException exception) {
                    wordOperatorExceptionAtomicReference.set(exception);
                    return null;
                }
                languagesLocal.removeIf(languageEntry -> languageEntry.getName() == null);
                languageDao.addLanguages(languagesLocal.toArray(new Language[0]));
            }
            return languagesLocal;
        });
        try {
            var languages = futureLanguages.get();
            if (wordOperatorExceptionAtomicReference.get() != null) {
                Toaster.make(context, context.getString(R.string.no_lang_retrieved), wordOperatorExceptionAtomicReference.get());
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
