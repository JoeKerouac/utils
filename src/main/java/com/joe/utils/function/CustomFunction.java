package com.joe.utils.function;

/**
 * 函数，替代{@link Runnable}，不同的是方法可以抛出异常
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月21日 18:38 JoeKerouac Exp $
 */
public interface CustomFunction {
    /**
     * 执行函数
     * 
     * @throws Throwable
     *             异常
     */
    void run() throws Throwable;
}
