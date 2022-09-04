package com.yk.common.model.book;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.yk.common.utils.ApplicationContext;
import com.yk.common.utils.ThreadOperator;

import java.io.FileOutputStream;
import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.S)
public class BookServiceHelper {

    public static void updateLatestBookPath(Context context, String bookPath) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput("lastBook.txt", Context.MODE_PRIVATE);
            fileOutputStream.write(bookPath.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            // it's important, but not critical, so exception is not rethrown
            Log.e("BookService", "Error on latest book path update");
        }
    }

    static void createPersistenceBook(BookService bookService) {
        ThreadOperator.addToQueue(() -> ApplicationContext.getContext().getAppDatabaseAbstract().bookDao().addNewBook(bookService.getBook()));
    }

    public static void updatePersistenceBook(BookService bookService) {
        ThreadOperator.addToQueue(() -> ApplicationContext.getContext().getAppDatabaseAbstract().bookDao().updateBook(bookService.getBook()));
    }

    public static void removePersistenceBook(Book book) {
        ThreadOperator.addToQueue(() -> ApplicationContext.getContext().getAppDatabaseAbstract().bookDao().deleteBook(book));
    }

    static Book uploadBookFromDatabase(String path) throws BookServiceException {
        return (Book) ThreadOperator
                .executeSingle(p -> ApplicationContext.getContext().getAppDatabaseAbstract().bookDao().getBookByPath(p), path,
                        () -> {
                            throw new BookServiceException("Error on loading book from database");
                        });
    }

}
