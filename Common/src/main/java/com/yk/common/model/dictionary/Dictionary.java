package com.yk.common.model.dictionary;

import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Dictionary definitions
 */
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@Getter
public class Dictionary {

    public final static String MAIN_TRANSLATION = "Main";

    @Embedded
    private final OriginWord originWord;
    @Relation(parentColumn = "id", entityColumn = "origin_word_id")
    private final List<WordTranslation> translations;
    @Relation(parentColumn = "id", entityColumn = "origin_word_id")
    private final List<WordDefinition> definitions;

    @RequiresApi(api = Build.VERSION_CODES.S)
    public String getMainTranslation() {
        return translations.stream().filter(wordTranslation -> wordTranslation.getPartOfSpeech().equals(MAIN_TRANSLATION))
                .findFirst().orElseGet(() -> new WordTranslation(0, 0, MAIN_TRANSLATION, "")).getTranslation();
    }

}
