package com.joe.utils.exception;

/**
 * 对IOExcetion的包装
 *
 * @author joe
 * @version 2018.07.19 18:02
 */
public class IOExceptionWrapper extends UtilsException {
    public IOExceptionWrapper() {
        super();
    }

    public IOExceptionWrapper(String message) {
        super(message);
    }

    public IOExceptionWrapper(String message, Throwable cause) {
        super(message, cause);
    }

    public IOExceptionWrapper(Throwable cause) {
        super(cause);
    }

    protected IOExceptionWrapper(String message, Throwable cause, boolean enableSuppression,
                                 boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
