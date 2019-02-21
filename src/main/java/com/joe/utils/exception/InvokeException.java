package com.joe.utils.exception;

import com.joe.utils.exception.UtilsException;

/**
 * 调用异常
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年02月21日 19:25 JoeKerouac Exp $
 */
public class InvokeException extends UtilsException {
    public InvokeException() {
    }

    public InvokeException(String message) {
        super(message);
    }

    public InvokeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvokeException(Throwable cause) {
        super(cause);
    }

    public InvokeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
