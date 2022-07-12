package com.yk.common.model.dictionary;

import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Word definition
 */
@RequiresApi(api = Build.VERSION_CODES.S)
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@Entity(tableName = "word_definition")
@Getter
@Setter
public class WordDefinition {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "origin_word_id")
    private int originWordId;
    @ColumnInfo(name = "part_of_speech")
    private String partOfSpeech;
    @ColumnInfo(name = "definition")
    private String definition;
}
