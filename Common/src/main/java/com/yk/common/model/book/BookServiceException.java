package com.yk.common.model.book;

public class BookServiceException extends Exception {
    BookServiceException(String message, Throwable throwable) {
        super(message, throwable);
    }

    BookServiceException(String message) {
        super(message);
    }
}
