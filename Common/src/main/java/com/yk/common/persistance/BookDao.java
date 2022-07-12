package com.yk.common.persistance;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.yk.common.model.book.Book;

import java.util.List;

/**
 * Book database access
 */
@Dao
public interface BookDao {

    /**
     * Method to select all books
     *
     * @return list of books
     */
    @Query("SELECT * FROM book")
    List<Book> getAllBooks();

    /**
     * Method to select a book by path
     *
     * @return book
     */
    @Query("SELECT * FROM book where file_path = :path")
    Book getBookByPath(String path);

    /**
     * Method to add new book
     */
    @Insert
    void addNewBook(Book book);

    /**
     * Method to update book
     */
    @Update
    void updateBook(Book book);

    /**
     * Method to delete book
     */
    @Delete
    void deleteBook(Book book);

}
