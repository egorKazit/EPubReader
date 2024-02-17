package com.yk.common.service.book;

import android.content.Context;

import com.yk.common.context.ApplicationContext;
import com.yk.common.model.book.Book;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Book pool.
 * It contains already loaded book and updates it on adding or deleting
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BookPool {

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
     * @return book
     * @throws BookServiceException exception on book loading
     */
    public static Book uploadBook(String path, boolean[] isNewBook) throws BookServiceException {
        Book bookToReturn = BOOKS.stream().filter(book -> book.getFilePath().equals(path)).findFirst().orElse(null);
        if (bookToReturn == null) {
            BookService bookService = BookService.initFromPath(path);
            bookToReturn = bookService.getBook();
            BOOKS.add(0, bookToReturn);
            if (isNewBook.length == 1) {
                isNewBook[0] = true;
            }
        }
        return bookToReturn;
    }

    /**
     * Method to remove book by id
     *
     * @param id book id
     */
    public static void removeBook(int id) {
        BookServiceHelper.removePersistenceBook(BOOKS.get(id));
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
