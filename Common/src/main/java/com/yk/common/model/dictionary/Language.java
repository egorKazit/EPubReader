package com.yk.common.model.dictionary;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@Entity(tableName = "languages")
@Getter
@Setter
public class Language {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "language")
    private String language;
    @ColumnInfo(name = "name")
    private String name;
}