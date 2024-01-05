package com.yk.common.http;

public class WordOperatorException extends Exception {
    WordOperatorException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public WordOperatorException(String message) {
        super(message);
    }

}
