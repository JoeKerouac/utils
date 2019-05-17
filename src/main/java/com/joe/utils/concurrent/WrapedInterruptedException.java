package com.joe.utils.concurrent;

import com.joe.utils.exception.UtilsException;

/**
 * InterruptedException的包装类
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年04月10日 20:41 JoeKerouac Exp $
 */
public class WrapedInterruptedException extends UtilsException {

    public WrapedInterruptedException(InterruptedException e) {
        super(e);
    }
}
