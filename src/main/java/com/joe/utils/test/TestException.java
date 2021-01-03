package com.joe.utils.test;

import com.joe.utils.exception.UtilsException;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月21日 18:40 JoeKerouac Exp $
 */
public class TestException extends UtilsException {

    private static final long serialVersionUID = -6566052855576257679L;

    public TestException() {
        super();
    }

    public TestException(String message) {
        super(message);
    }

    public TestException(String message, Throwable cause) {
        super(cause, message);
    }

    public TestException(Throwable cause) {
        super(cause);
    }

    protected TestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(cause, enableSuppression, writableStackTrace, message);
    }
}
