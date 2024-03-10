package com.yk.contentviewer.maincontent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.yk.common.http.WordOperatorException;
import com.yk.common.http.WordTranslator;
import com.yk.common.service.book.BookService;
import com.yk.common.service.book.BookServiceException;
import com.yk.common.service.dictionary.DictionaryService;
import com.yk.common.service.dictionary.LanguageService;
import com.yk.common.utils.Toaster;
import com.yk.contentviewer.R;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.AllArgsConstructor;

/**
 * Class as one place for all JS handlers
 */

@AllArgsConstructor
public final class ContentViewerJSHandler {

    public static final String MAIN = "Main";
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
        translateWithProgressBar(originalWord);
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
            var translatedWord = ((TextView) activity.findViewById(R.id.contentViewerTranslatedWord));
            try {
                translatedWord.setText(WordTranslator.resolveTranslation(originPhrase, BookService.getBookService().getLanguage(),
                                LanguageService.getInstance().getLanguage()).getTranslations()
                        .stream().filter(wordTranslation -> wordTranslation.getPartOfSpeech().equals(MAIN))
                        .findAny()
                        .orElseThrow(() -> new WordOperatorException(activity.getString(R.string.no_translation))).getTranslation());
            } catch (WordOperatorException | BookServiceException e) {
                translatedWord.setText(e.getMessage());
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
            translateWithProgressBar(originalPhrase);
        } else {
            final AlertDialog alertDialog =
                    new AlertDialog.Builder(activity)
                            .setMessage(originTextTrim)
                            .setPositiveButton(R.string.translate_button, null)
                            .setNegativeButton(R.string.close_button, null).show();
            Button translateButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            translateButton.setOnClickListener(dialog -> {
                try {
                    alertDialog.setMessage(String.join(System.lineSeparator(), originTextTrim,
                            WordTranslator.resolveTranslation(originalPhrase, BookService.getBookService().getLanguage(),
                                            LanguageService.getInstance().getLanguage())
                                    .getTranslations().stream().filter(wordTranslation -> wordTranslation.getPartOfSpeech().equals(MAIN))
                                    .findAny()
                                    .orElseThrow(() -> new WordOperatorException(activity.getString(R.string.no_translation))).getTranslation()));
                } catch (WordOperatorException | BookServiceException e) {
                    alertDialog.setMessage(String.join(System.lineSeparator(), originTextTrim, e.getMessage()));
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
    public void handleSelectedImage(@NonNull String imageUrl) {
        try {
            ContentViewerImageDialog.openImageDialog(activity, imageUrl);
        } catch (URISyntaxException | BookServiceException | IOException e) {
            Toaster.make(activity.getApplicationContext(), R.string.can_not_load_image, e);
        }
    }

    private void translateWithProgressBar(String original) {
        var translatedWord = ((TextView) activity.findViewById(R.id.contentViewerTranslatedWord));
        var translationProgressBar = (ProgressBar) activity.findViewById(R.id.contentViewerTranslationProgressBar);
        activity.runOnUiThread(() -> {
//            translatedWord.setText("");
//            translatedWord.setVisibility(View.INVISIBLE);
            translationProgressBar.setVisibility(View.VISIBLE);
        });
        // set translation text
        wordTranslationThreadOperator.submit(() -> {
            var dictionary = DictionaryService.getInstance().getDictionary(original.toLowerCase(Locale.ROOT));
            String translation = DictionaryService.getMainTranslation(dictionary);
            activity.runOnUiThread(() -> {
                translationProgressBar.setVisibility(View.INVISIBLE);
                translatedWord.setText(String.format("%s - %s", original.toLowerCase(Locale.ROOT), translation));
                translatedWord.setSelected(true);
                translatedWord.setVisibility(View.VISIBLE);
            });
        });
    }

}
