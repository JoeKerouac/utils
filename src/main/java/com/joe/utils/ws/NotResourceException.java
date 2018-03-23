package com.joe.utils.ws;

/**
 * 如果类或者调用的方法不是资源将会抛出该异常
 *
 * @author joe
 */
public class NotResourceException extends RuntimeException {
    public NotResourceException(Object obj) {
        super(obj + "不是一个资源，请检查");
    }
}
