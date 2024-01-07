package com.yk.contentviewer.maincontent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.yk.common.constants.GlobalConstants;
import com.yk.common.http.WordOperatorException;
import com.yk.common.http.WordTranslator;
import com.yk.common.service.book.BookService;
import com.yk.common.service.book.BookServiceException;
import com.yk.common.service.dictionary.DictionaryService;
import com.yk.common.service.dictionary.LanguageService;
import com.yk.contentviewer.R;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

/**
 * Class as one place for all JS handlers
 */

@AllArgsConstructor
public class ContentViewerJSHandler {

    private final Activity activity;
    private final ExecutorService wordTranslationThreadOperator = Executors.newFixedThreadPool(20);
    private final ExecutorService phraseTranslationThreadOperator = Executors.newFixedThreadPool(20);

    /**
     * Method to retrieve the word, translate it and put in correct place
     *
     * @param originalWord original word
     */
    public void handleSelectedWord(@NonNull String originalWord) {
        if (activity.findViewById(R.id.contentViewerTranslatedWord).getVisibility() != View.VISIBLE) {
            return;
        }
        // set translation text
        wordTranslationThreadOperator.submit(() -> {
            var dictionary = DictionaryService.getInstance().getDictionary(originalWord.toLowerCase(Locale.ROOT));
            String translation = DictionaryService.getMainTranslation(dictionary);
            activity.runOnUiThread(() -> {
                var translatedWord = ((TextView) activity.findViewById(R.id.contentViewerTranslatedWord));
                translatedWord.setText(String.format("%s - %s", originalWord.toLowerCase(Locale.ROOT), translation));
                translatedWord.setSelected(true);
            });
        });
    }

    /**
     * Method to retrieve the phrase around selected word, translate it and put in correct place
     *
     * @param originPhrase origin phrase
     */
    @SuppressLint("SetTextI18n")
    public void handleContextOfSelectedWord(@NonNull String originPhrase) {
        // set translation text
        if (activity.findViewById(R.id.contentViewerTranslatedContext).getVisibility() != View.VISIBLE) {
            return;
        }
        phraseTranslationThreadOperator.submit(() -> {
            try {
                ((TextView) activity.findViewById(R.id.contentViewerTranslatedContext))
                        .setText(WordTranslator.resolveTranslation(originPhrase, BookService.getBookService().getLanguage(),
                                        LanguageService.getInstance().getLanguage()).getTranslations()
                                .stream().filter(wordTranslation -> wordTranslation.getPartOfSpeech().equals("Main"))
                                .findAny()
                                .orElseThrow(() -> new WordOperatorException("No translation")).getTranslation());
            } catch (WordOperatorException | BookServiceException e) {
                ((TextView) activity.findViewById(R.id.contentViewerTranslatedContext))
                        .setText(GlobalConstants.ERROR_ON_TRANSLATE + e.getMessage());
            }
        });
    }

    /**
     * Method to retrieve the phrase that is selected, translate it and bring popup with translate option
     *
     * @param originalPhrase original phrase
     */
    public void handleSelectedPhrase(@NonNull String originalPhrase) {
        String originTextTrim = originalPhrase.trim();
        if (!originTextTrim.isEmpty() && !originTextTrim.contains(" ")) {
            // set translation text
            wordTranslationThreadOperator.submit(() ->
                    ((TextView) activity.findViewById(R.id.contentViewerTranslatedWord))
                            .setText(DictionaryService.getMainTranslation(DictionaryService.getInstance().getDictionary(originalPhrase))));
            new Thread(() -> ((TextView) activity.findViewById(R.id.contentViewerTranslatedWord))
                    .setText(DictionaryService.getMainTranslation(DictionaryService.getInstance().getDictionary(originalPhrase)))).start();
        } else {
            final AlertDialog alertDialog =
                    new AlertDialog.Builder(activity)
                            .setMessage(originTextTrim)
                            .setPositiveButton("Translate", null)
                            .setNegativeButton("Close", null).show();
            Button translateButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            translateButton.setOnClickListener(dialog -> {
                try {
                    alertDialog.setMessage(String.join("\n", originTextTrim,
                            WordTranslator.resolveTranslation(originalPhrase, BookService.getBookService().getLanguage(),
                                            LanguageService.getInstance().getLanguage())
                                    .getTranslations().stream().filter(wordTranslation -> wordTranslation.getPartOfSpeech().equals("Main"))
                                    .findAny()
                                    .orElseThrow(() -> new WordOperatorException("No translation")).getTranslation()));
                } catch (WordOperatorException | BookServiceException e) {
                    alertDialog.setMessage(String.join("\n", originTextTrim, GlobalConstants.ERROR_ON_TRANSLATE + e.getMessage()));
                }
                translateButton.setVisibility(View.GONE);
            });
        }

    }

    /**
     * Method to zoom image on click
     *
     * @param imageUrl image url
     */
    @SuppressLint("ClickableViewAccessibility")
    @SneakyThrows
    public void handleSelectedImage(@NonNull String imageUrl) {
        ContentViewerImageDialog.openImageDialog(activity, imageUrl);
    }

}
