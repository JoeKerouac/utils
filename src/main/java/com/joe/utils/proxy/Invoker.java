package com.joe.utils.proxy;

/**
 * 执行者
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月10日 23:22 JoeKerouac Exp $
 */
public interface Invoker {

    /**
     * 调用
     * @return 调用结果
     * @throws Throwable 异常
     */
    Object call() throws Throwable;
}
