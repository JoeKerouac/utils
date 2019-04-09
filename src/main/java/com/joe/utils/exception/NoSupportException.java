package com.joe.utils.exception;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年04月08日 20:31 JoeKerouac Exp $
 */
public class NoSupportException extends UtilsException {
    public NoSupportException() {
        super();
    }

    public NoSupportException(String message) {
        super(message);
    }

    public NoSupportException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSupportException(Throwable cause) {
        super(cause);
    }

    protected NoSupportException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
