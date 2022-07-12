package com.yk.bookviewer.ui.dictionary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.yk.bookviewer.R;
import com.yk.common.model.dictionary.Dictionary;
import com.yk.common.model.dictionary.DictionaryPool;
import com.yk.common.model.dictionary.PartOfSpeech;
import com.yk.common.model.dictionary.WordDefinition;

import java.util.List;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Dictionary Translate Operator.
 * It gets all definitions and inflates in "definitions" section
 */
@RequiresApi(api = Build.VERSION_CODES.S)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class DictionaryDefinitionOperator {
    private final View rootView;
    private final String wordToTranslate;

    void start() {
        // inflate in a separate thread as definition can be not load previously and there's no reason to
        new Thread(() -> {

            // Get all definitions
            Optional<Dictionary> dictionaryWithDefinitions = DictionaryPool.getDictionaries().stream().filter(dictionary -> dictionary.getOriginWord()
                    .getOriginWord().equals(wordToTranslate)).findFirst();
            if (!dictionaryWithDefinitions.isPresent()) {
                return;
            }

            if (dictionaryWithDefinitions.get().getDefinitions() != null) {
                final List<WordDefinition> finalDefinitions = dictionaryWithDefinitions.get().getDefinitions();
                ((Activity) rootView.getContext()).runOnUiThread(() -> {
                    // inflate new section for definitions(as list) and append each definition as separate line
                    LinearLayout definitionLayout = rootView.findViewById(R.id.dictionaryDetailsDefinitions);
                    finalDefinitions.forEach(singleDefinition -> {
                        // create new view
                        @SuppressLint("InflateParams") View definitionLine = LayoutInflater.from(rootView.getContext())
                                .inflate(R.layout.fragment_dictionary_definition, null, false);
                        // set type of speech and text
                        ((TextView) definitionLine.findViewById(R.id.dictionaryDefinitionType))
                                .setText(PartOfSpeech.getAbbreviationFromName(singleDefinition.getPartOfSpeech()));
                        ((TextView) definitionLine.findViewById(R.id.dictionaryDefinitionDefinition))
                                .setText(singleDefinition.getDefinition());
                        // append new view
                        definitionLayout.addView(definitionLine);
                    });
                });
            }
        }).start();
    }

}
