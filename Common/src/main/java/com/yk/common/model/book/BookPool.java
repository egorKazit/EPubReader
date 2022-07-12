package com.yk.common.model.book;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.yk.common.utils.ApplicationContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Book pool.
 * It contains already loaded book and updates it on adding or deleting
 */
@RequiresApi(api = Build.VERSION_CODES.S)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookPool {

    private static final List<Book> BOOKS = new ArrayList<>();

    /**
     * Method to upload all books from database
     */
    public static void uploadBooks() {
        BOOKS.clear();
        BOOKS.addAll(ApplicationContext.getContext().getAppDatabaseAbstract().bookDao().getAllBooks());
        BOOKS.sort(Comparator.comparing(Book::getAddingDate).reversed());
    }

    /**
     * Method to get book by id
     *
     * @param id book id
     * @return book
     */
    public static Book getBook(int id) {
        return BOOKS.get(id);
    }

    /**
     * Method to upload book from path to book
     *
     * @param path path to book
     * @return true if book is new
     * @throws BookServiceException exception on book loading
     */
    public static boolean uploadBook(String path) throws BookServiceException {
        if (BOOKS.stream().noneMatch(book -> book.getFilePath().equals(path))) {
            BookService bookService = BookService.initFromPath(path);
            BOOKS.add(0, bookService.getBook());
            return true;
        }
        return false;
    }

    /**
     * Method to remove book by id
     *
     * @param id book id
     */
    public static void removeBook(int id) {
        BookService.removeBook(BOOKS.get(id));
        BOOKS.remove(BOOKS.get(id));
    }

    /**
     * Method to get pool size
     *
     * @return pool size
     */
    public static int getSize() {
        return BOOKS.size();
    }

}
