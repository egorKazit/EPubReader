package com.yk.common.model.dictionary;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Origin word definition
 */

@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@Entity(tableName = "origin_word", indices = {@Index(value = {"origin_word", "source_language", "target_language"}, unique = true)})
@Getter
@Setter
public class OriginWord {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "origin_word")
    private String originWord;
    @ColumnInfo(name = "source_language")
    private String sourceLanguage;
    @ColumnInfo(name = "target_language")
    private String targetLanguage;
}
