package com.yk.fileexplorer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.yk.common.service.book.BookService;
import com.yk.common.service.book.BookServiceException;

import java.io.InputStream;

import lombok.Getter;

@Getter
public final class FileExplorerItem {

    private final String fileName;
    private final String filePath;
    private final boolean isFile;
    private Bitmap bitmap;
    private String title;

    
    public FileExplorerItem(String fileName, String filePath, boolean isFile) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.isFile = isFile;

        if (filePath == null)
            return;

        try {
            BookService bookService = BookService.buildFromPath(filePath);
            InputStream inputStream = bookService.getCover();
            title = bookService.getTitle();
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (BookServiceException ignored) {
            Log.e("", "");
        }

    }

}
