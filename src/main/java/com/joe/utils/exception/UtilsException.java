package com.joe.utils.exception;

import com.joe.utils.common.string.StringFormater;

/**
 * 工具包异常
 *
 * @author joe
 * @version 2018.07.18 10:25
 */
public class UtilsException extends RuntimeException {

    private static final long serialVersionUID = 4416969498866543782L;

    public UtilsException() {
        super();
    }

    public UtilsException(String msgTemp, Object... args) {
        super(StringFormater.simpleFormat(msgTemp, args));
    }

    public UtilsException(Throwable cause, String message, Object... args) {
        super(StringFormater.simpleFormat(message, args), cause);
    }

    public UtilsException(Throwable cause) {
        super(cause);
    }

    protected UtilsException(Throwable cause, boolean enableSuppression, boolean writableStackTrace,
                             String message, Object... args) {
        super(StringFormater.simpleFormat(message, args), cause, enableSuppression,
            writableStackTrace);
    }
}
