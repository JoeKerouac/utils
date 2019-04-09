package com.joe.utils.serialize;

import com.joe.utils.exception.UtilsException;

/**
 * 序列化异常
 *
 * @author joe
 */
public class SerializeException extends UtilsException {

    private static final long serialVersionUID = 6354099068931120268L;

    public SerializeException(Throwable ex) {
        super("序列化异常", ex);
    }

    public SerializeException(String message) {
        super("序列化异常[" + message + "]");
    }
}
