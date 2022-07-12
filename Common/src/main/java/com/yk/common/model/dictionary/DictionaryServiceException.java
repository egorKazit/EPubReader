package com.yk.common.model.dictionary;

/**
 * Dictionary service exception
 */
public class DictionaryServiceException extends Exception {

    /**
     * Main constructor
     *
     * @param message   exception message
     * @param throwable cause exception
     */
    DictionaryServiceException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
