package com.yk.common.model.dictionary;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Word translation
 */

@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@Entity(tableName = "word_translation")
@Getter
@Setter
public final class WordTranslation {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "origin_word_id")
    private int originWordId;

    @ColumnInfo(name = "part_of_speech")
    private String partOfSpeech;
    @ColumnInfo(name = "translation")
    private String translation;
}
