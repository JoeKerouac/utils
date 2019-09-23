package com.joe.utils.exception;

public class PoolObjectHolderClosedException extends UtilsException {

    private static final long serialVersionUID = -8618479485878902136L;

    public PoolObjectHolderClosedException() {
        super();
    }

    public PoolObjectHolderClosedException(String message) {
        super(message);
    }

    public PoolObjectHolderClosedException(String message, Throwable cause) {
        super(cause, message);
    }

    public PoolObjectHolderClosedException(Throwable cause) {
        super(cause);
    }

    protected PoolObjectHolderClosedException(String message, Throwable cause,
                                              boolean enableSuppression,
                                              boolean writableStackTrace) {
        super(cause, enableSuppression, writableStackTrace, message);
    }
}
