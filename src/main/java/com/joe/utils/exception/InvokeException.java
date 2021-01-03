package com.joe.utils.exception;

/**
 * 调用异常
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年02月21日 19:25 JoeKerouac Exp $
 */
public class InvokeException extends UtilsException {

    private static final long serialVersionUID = 5078472212951684350L;

    public InvokeException() {}

    public InvokeException(String message) {
        super(message);
    }

    public InvokeException(String message, Throwable cause) {
        super(cause, message);
    }

    public InvokeException(Throwable cause) {
        super(cause);
    }

    public InvokeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(cause, enableSuppression, writableStackTrace, message);
    }
}
