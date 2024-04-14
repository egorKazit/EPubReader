package com.yk.fileexplorer;

import com.yk.common.service.book.BookService;
import com.yk.common.service.book.BookServiceException;
import com.yk.common.service.cache.CacheService;

import lombok.Getter;

@Getter
public final class FileExplorerItem {

    private final String fileName;
    private final String filePath;
    private final boolean isFile;
    private byte[] bitmap;
    private String title;


    public FileExplorerItem(String fileName, String filePath, boolean isFile) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.isFile = isFile;
    }

    public byte[] getCover() {
        if (bitmap != null)
            return bitmap;
        try {
            BookService bookService = BookService.buildFromPath(filePath);
            title = bookService.getTitle();
            return bitmap = CacheService.Instance.INSTANCE.cacheService.loadBitmapBytes(filePath, bookService.getCover());
        } catch (BookServiceException ignored) {
            return null;
        }
    }

    public String fetchTitle() {
        if (title != null)
            return title;
        try {
            BookService bookService = BookService.buildFromPath(filePath);
            bitmap = CacheService.Instance.INSTANCE.cacheService.loadBitmapBytes(filePath, bookService.getCover());
            return title = bookService.getTitle();
        } catch (BookServiceException ignored) {
            return null;
        }
    }
}
