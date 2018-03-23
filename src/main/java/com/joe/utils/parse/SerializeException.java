package com.joe.utils.parse;

/**
 * 序列化异常
 *
 * @author joe
 */
public class SerializeException extends RuntimeException {
    private static final long serialVersionUID = 6354099068931120268L;

    public SerializeException(Throwable ex) {
        super("序列化异常", ex);
    }

    public SerializeException(String message) {
        super("序列化异常[" + message + "]");
    }
}
