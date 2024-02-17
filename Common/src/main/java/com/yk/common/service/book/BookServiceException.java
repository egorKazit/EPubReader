package com.yk.common.service.book;

public final class BookServiceException extends Exception {
    BookServiceException(String message, Throwable throwable) {
        super(message, throwable);
    }

    BookServiceException(String message) {
        super(message);
    }
}
