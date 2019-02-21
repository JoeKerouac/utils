package com.joe.utils.exception;

/**
 * telnet异常
 *
 * @author joe
 * @version 2018.07.19 18:14
 */
public class TelnetException extends UtilsException {
    public TelnetException() {
        super();
    }

    public TelnetException(String message) {
        super(message);
    }

    public TelnetException(String message, Throwable cause) {
        super(message, cause);
    }

    public TelnetException(Throwable cause) {
        super(cause);
    }

    protected TelnetException(String message, Throwable cause, boolean enableSuppression,
                              boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
