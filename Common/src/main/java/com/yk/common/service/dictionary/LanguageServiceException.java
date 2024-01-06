package com.yk.common.service.dictionary;

/**
 * Language service exception
 */
public class LanguageServiceException extends Exception {

    /**
     * Main constructor
     *
     * @param message   exception message
     * @param throwable cause exception
     */
    LanguageServiceException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
