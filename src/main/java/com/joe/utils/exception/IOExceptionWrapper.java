package com.joe.utils.exception;

/**
 * 对IOExcetion的包装
 *
 * @author joe
 * @version 2018.07.19 18:02
 */
public class IOExceptionWrapper extends UtilsException {

    private static final long serialVersionUID = -3222997623900804631L;

    public IOExceptionWrapper() {
        super();
    }

    public IOExceptionWrapper(String message) {
        super(message);
    }

    public IOExceptionWrapper(String message, Throwable cause) {
        super(cause, message);
    }

    public IOExceptionWrapper(Throwable cause) {
        super(cause);
    }

    protected IOExceptionWrapper(String message, Throwable cause, boolean enableSuppression,
                                 boolean writableStackTrace) {
        super(cause, enableSuppression, writableStackTrace, message);
    }
}
