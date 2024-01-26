package com.yk.common.service.book;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.yk.common.context.ApplicationContext;
import com.yk.common.model.book.Book;
import com.yk.common.persistance.AppDatabaseFactory;
import com.yk.common.utils.Toaster;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;


public class BookServiceHelper {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void updateLatestBookPath(@NonNull Context context, @NonNull String bookPath) {
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
            throw new BookServiceException("Can not load book", e);
        }
    }


}
