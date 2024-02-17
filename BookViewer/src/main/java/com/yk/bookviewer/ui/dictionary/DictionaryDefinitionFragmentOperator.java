package com.yk.bookviewer.ui.dictionary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.yk.bookviewer.R;
import com.yk.common.constants.PartOfSpeech;

import java.util.Map;
import java.util.Set;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Dictionary Translate Operator.
 * It gets all definitions and inflates in "definitions" section
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class DictionaryDefinitionFragmentOperator {

    private final Activity activity;

    void inflateValuesWithPartOfSpeech(int layoutId, @NonNull Set<Map.Entry<String, String>> valuesPerPartOfSpeech) {
        // inflate new section for definitions(as list) and append each definition as separate line
        LinearLayout definitionLayout = activity.findViewById(layoutId);
        valuesPerPartOfSpeech.forEach(valuePerPartOfSpeech -> {
            // create new view
            @SuppressLint("InflateParams")
            View definitionLine = LayoutInflater.from(activity)
                    .inflate(R.layout.fragment_dictionary_details_item, null, false);
            // set type of speech and text
            TextView partOfSpeech = definitionLine.findViewById(R.id.dictionaryDetailsPartOfSpeech);
            partOfSpeech.setText(PartOfSpeech.getAbbreviationFromName(valuePerPartOfSpeech.getValue()));
            TextView value = definitionLine.findViewById(R.id.dictionaryDetailsValue);
            value.setText(valuePerPartOfSpeech.getKey());
            // append new view
            definitionLayout.addView(definitionLine);
        });
    }

}
