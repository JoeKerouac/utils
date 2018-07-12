package com.joe.utils.secure.exception;

/**
 * @author joe
 * @version 2018.05.10 15:36
 */
public class SecureException extends RuntimeException{
    public SecureException() {
        super();
    }

    public SecureException(String message) {
        super(message);
    }

    public SecureException(String message, Throwable cause) {
        super(message, cause);
    }

    public SecureException(Throwable cause) {
        super(cause);
    }

    protected SecureException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
