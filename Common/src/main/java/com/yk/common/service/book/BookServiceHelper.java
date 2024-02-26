package com.yk.common.service.book;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.yk.common.R;
import com.yk.common.context.ApplicationContext;
import com.yk.common.model.book.Book;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public final class BookServiceHelper {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void updateLatestBookPath(@NonNull Context context, @NonNull String bookPath) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(BookService.LAST_BOOK_TXT, Context.MODE_PRIVATE);
            fileOutputStream.write(bookPath.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            // it's important, but not critical, so exception is not rethrown
            Log.e(BookService.SERVICE_TAG, ApplicationContext.getContext().getString(R.string.error_on_book_update));
        }
    }

    static void createPersistenceBook(BookService bookService) {
        executorService.submit(() -> {
            var id = ApplicationContext.getContext().getAppDatabaseAbstract().bookDao().addNewBook(bookService.getBook());
            bookService.getBook().setId((int) id);
            return true;
        });
    }

    public static void updatePersistenceBook(BookService bookService) {
        executorService.submit(() -> ApplicationContext.getContext().getAppDatabaseAbstract().bookDao().updateBook(bookService.getBook()));
    }

    public static void removePersistenceBook(Book book) {
        executorService.submit(() -> ApplicationContext.getContext().getAppDatabaseAbstract().bookDao().deleteBook(book));
    }

    static Book uploadBookFromDatabase(String path) throws BookServiceException {
        var futureBook = executorService.submit(() -> ApplicationContext.getContext().getAppDatabaseAbstract().bookDao().getBookByPath(path));
        try {
            return futureBook.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new BookServiceException(ApplicationContext.getContext().getString(R.string.can_not_load_book), e);
        }
    }


}
