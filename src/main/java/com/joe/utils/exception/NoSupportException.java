package com.joe.utils.exception;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年04月08日 20:31 JoeKerouac Exp $
 */
public class NoSupportException extends UtilsException {

    private static final long serialVersionUID = 394239663067765178L;

    public NoSupportException() {
        super();
    }

    public NoSupportException(String message) {
        super(message);
    }

    public NoSupportException(String message, Throwable cause) {
        super(cause, message);
    }

    public NoSupportException(Throwable cause) {
        super(cause);
    }

    protected NoSupportException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(cause, enableSuppression, writableStackTrace, message);
    }
}
