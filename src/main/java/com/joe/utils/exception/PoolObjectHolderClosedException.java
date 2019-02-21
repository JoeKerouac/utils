package com.joe.utils.exception;

public class PoolObjectHolderClosedException extends UtilsException {
    public PoolObjectHolderClosedException() {
        super();
    }

    public PoolObjectHolderClosedException(String message) {
        super(message);
    }

    public PoolObjectHolderClosedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PoolObjectHolderClosedException(Throwable cause) {
        super(cause);
    }

    protected PoolObjectHolderClosedException(String message, Throwable cause,
                                              boolean enableSuppression,
                                              boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
