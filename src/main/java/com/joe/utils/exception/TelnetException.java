package com.joe.utils.exception;

/**
 * telnet异常
 *
 * @author joe
 * @version 2018.07.19 18:14
 */
public class TelnetException extends UtilsException {

    private static final long serialVersionUID = 5640530092124753574L;

    public TelnetException() {
        super();
    }

    public TelnetException(String message) {
        super(message);
    }

    public TelnetException(String message, Throwable cause) {
        super(cause, message);
    }

    public TelnetException(Throwable cause) {
        super(cause);
    }

    protected TelnetException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(cause, enableSuppression, writableStackTrace, message);
    }
}
