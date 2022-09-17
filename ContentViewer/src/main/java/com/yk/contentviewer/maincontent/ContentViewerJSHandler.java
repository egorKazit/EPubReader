package com.yk.contentviewer.maincontent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.yk.common.constants.GlobalConstants;
import com.yk.common.model.dictionary.DictionaryPool;
import com.yk.common.utils.learning.WordOperatorException;
import com.yk.common.utils.learning.WordTranslator;
import com.yk.contentviewer.R;

import java.util.Locale;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

/**
 * Class as one place for all JS handlers
 */
@RequiresApi(api = Build.VERSION_CODES.S)
@AllArgsConstructor
public class ContentViewerJSHandler {

    private final ContentViewerWebView contentViewerWebView;

    /**
     * Method to retrieve the word, translate it and put in correct place
     *
     * @param originalWord original word
     */
    public void handleSelectedWord(@NonNull String originalWord) {
        if (((Activity) contentViewerWebView.getContext()).findViewById(R.id.contentViewerTranslatedWord).getVisibility() != View.VISIBLE) {
            return;
        }
        // set translation text
        new Thread(() -> {
            String translation = DictionaryPool.getWordTranslation(originalWord.toLowerCase(Locale.ROOT));
            ((Activity) contentViewerWebView.getContext()).runOnUiThread(() ->
                    ((TextView) ((Activity) contentViewerWebView.getContext()).findViewById(R.id.contentViewerTranslatedWord))
                            .setText(String.format("%s - %s", originalWord.toLowerCase(Locale.ROOT), translation)));
        }).start();
    }

    /**
     * Method to retrieve the phrase around selected word, translate it and put in correct place
     *
     * @param originPhrase origin phrase
     */
    @SuppressLint("SetTextI18n")
    public void handleContextOfSelectedWord(@NonNull String originPhrase) {
        // set translation text
        if (((Activity) contentViewerWebView.getContext()).findViewById(R.id.contentViewerTranslatedContext).getVisibility() != View.VISIBLE) {
            return;
        }
        new Thread(() -> {
            try {
                ((TextView) ((Activity) contentViewerWebView.getContext()).findViewById(R.id.contentViewerTranslatedContext))
                        .setText(new WordTranslator().translateText(originPhrase).get(0));
            } catch (WordOperatorException e) {
                ((TextView) ((Activity) contentViewerWebView.getContext()).findViewById(R.id.contentViewerTranslatedContext))
                        .setText(GlobalConstants.ERROR_ON_TRANSLATE + e.getMessage());
            }
        }).start();
    }

    /**
     * Method to retrieve the phrase that is selected, translate it and bring popup with translate option
     *
     * @param originalPhrase original phrase
     */
    public void handleSelectedPhrase(@NonNull String originalPhrase) {
        String originTextTrim = originalPhrase.trim();
        if (originTextTrim.length() > 0 && !originTextTrim.contains(" ")) {
            // set translation text
            new Thread(() -> ((TextView) ((Activity) contentViewerWebView.getContext()).findViewById(R.id.contentViewerTranslatedWord))
                    .setText(DictionaryPool.getWordTranslation(originalPhrase))).start();
        } else {
            final AlertDialog alertDialog =
                    new AlertDialog.Builder(contentViewerWebView.getContext())
                            .setMessage(originTextTrim)
                            .setPositiveButton("Translate", null)
                            .setNegativeButton("Close", null).show();
            Button translateButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            translateButton.setOnClickListener(dialog -> {
                try {
                    alertDialog.setMessage(String.join("\n", originTextTrim, new WordTranslator().translateText(originalPhrase).get(0)));
                } catch (WordOperatorException e) {
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
        ContentViewerImageDialog.openImageDialog(contentViewerWebView.getContext(), imageUrl);
    }

}
