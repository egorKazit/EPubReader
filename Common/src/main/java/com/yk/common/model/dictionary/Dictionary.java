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

    @Embedded
    private final OriginWord originWord;
    @Relation(parentColumn = "id", entityColumn = "origin_word_id")
    private final List<WordTranslation> translations;
    @Relation(parentColumn = "id", entityColumn = "origin_word_id")
    private final List<WordDefinition> definitions;

}
