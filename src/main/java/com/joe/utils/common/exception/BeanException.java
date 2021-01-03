package com.joe.utils.common.exception;

/**
 * Bean工具异常
 *
 * @author joe
 * @version 2018.06.12 14:55
 */
public class BeanException extends RuntimeException {
    public BeanException() {
        super();
    }

    public BeanException(String message) {
        super(message);
    }

    public BeanException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanException(Throwable cause) {
        super(cause);
    }

    protected BeanException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
