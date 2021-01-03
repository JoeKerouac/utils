package com.joe.utils.collection;

/**
 * 集合操作异常
 * 
 * @author joe
 *
 */
public class CollectionException extends RuntimeException {
    private static final long serialVersionUID = -1110595984530739346L;

    public CollectionException() {
        super();
    }

    public CollectionException(String message) {
        super(message);
    }

    public CollectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public CollectionException(Throwable cause) {
        super(cause);
    }

    protected CollectionException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
