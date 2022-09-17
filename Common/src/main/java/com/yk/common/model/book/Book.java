package com.yk.common.model.book;

import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Book definition
 */
@RequiresApi(api = Build.VERSION_CODES.S)
@Builder(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@AllArgsConstructor
@Entity(tableName = "book")
@Getter
@Setter
public class Book {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "adding_date")
    private Date addingDate;
    @ColumnInfo(name = "file_path")
    private String filePath;
    @ColumnInfo(name = "root_path")
    private String rootPath;
    @ColumnInfo(name = "title")
    private String title;
    @ColumnInfo(name = "current_chapter_number")
    private int currentChapterNumber;
    @ColumnInfo(name = "current_chapter_position")
    private int currentChapterPosition;
    @ColumnInfo(name = "cover")
    private String cover;
    @ColumnInfo(name = "creator")
    private String creator;
    @ColumnInfo(name = "text_size")
    private int textSize;
}
