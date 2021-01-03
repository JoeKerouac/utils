package com.joe.utils.proxy;

import com.joe.utils.exception.UtilsException;

/**
 * 代理异常
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月08日 19:56 JoeKerouac Exp $
 */
public class ProxyException extends UtilsException {

    private static final long serialVersionUID = -8445040102390079092L;

    public ProxyException() {}

    public ProxyException(String message) {
        super(message);
    }

    public ProxyException(String message, Throwable cause) {
        super(cause, message);
    }

    public ProxyException(Throwable cause) {
        super(cause);
    }

    public ProxyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(cause, enableSuppression, writableStackTrace, message);
    }
}
