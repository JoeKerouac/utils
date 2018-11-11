package com.joe.utils.reflect;

import com.joe.utils.exception.UtilsException;

/**
 * 反射异常
 *
 * @author joe
 * @version 2018.07.18 10:26
 */
public class ReflectException extends UtilsException {
    public ReflectException() {
        super();
    }

    public ReflectException(String message) {
        super(message);
    }

    public ReflectException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReflectException(Throwable cause) {
        super(cause);
    }

    protected ReflectException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
