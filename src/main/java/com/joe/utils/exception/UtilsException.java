package com.joe.utils.exception;

/**
 * 工具包异常
 *
 * @author joe
 * @version 2018.07.18 10:25
 */
public class UtilsException extends RuntimeException{
    public UtilsException() {
        super();
    }

    public UtilsException(String message) {
        super(message);
    }

    public UtilsException(String message, Throwable cause) {
        super(message, cause);
    }

    public UtilsException(Throwable cause) {
        super(cause);
    }

    protected UtilsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
