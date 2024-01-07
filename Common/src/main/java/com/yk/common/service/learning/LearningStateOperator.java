package com.yk.common.service.learning;

import android.content.Context;
import android.content.SharedPreferences;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Class to handle learning position
 */

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class LearningStateOperator {

    private final String WORD_POSITION_FILE = "learning.txt";
    private final String WORD_POSITION = "WORD_POSITION";

    /**
     * Method to update word position
     *
     * @param wordPosition word position
     */
    void setWordPosition(int wordPosition, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(WORD_POSITION_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(WORD_POSITION, wordPosition);
        editor.apply();

    }

    /**
     * Method to get word position
     *
     * @return word position
     */
    int getWordPosition(Context context) {
        return context.getSharedPreferences(WORD_POSITION_FILE, Context.MODE_PRIVATE).getInt(WORD_POSITION, 0);
    }

}
