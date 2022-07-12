package com.yk.fileexplorer;


import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.yk.common.model.book.BookService;
import com.yk.common.model.book.BookServiceException;

import java.io.InputStream;

@RequiresApi(api = Build.VERSION_CODES.S)
public class FileExplorerItemImageInflater {
    private final String filePath;
    private final TextView fileName;
    private final ImageView fileImage;

    FileExplorerItemImageInflater(String filePath, TextView fileName, ImageView fileImage) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileImage = fileImage;
    }

    void startInflating() {
        new Thread(() -> {
            try {
                BookService bookService = BookService.buildFromPath(filePath);
                InputStream inputStream = bookService.getCover();
                ((Activity) fileName.getContext()).runOnUiThread(() -> {
                    try {
                        fileName.setText(bookService.getTitle());
                    } catch (BookServiceException bookServiceException) {
                        bookServiceException.printStackTrace();
                    }
                    if (inputStream != null) {
                        fileImage.setImageBitmap(BitmapFactory.decodeStream(inputStream));

                    } else {
                        fileImage.setImageBitmap(BitmapFactory.decodeResource(fileName.getContext().getResources(),
                                R.drawable.ic_no_cover_foreground));
                    }
                });
            } catch (BookServiceException ignored) {
            }
        }).start();
    }

}
