package com.joe.utils.function;

/**
 * 可执行的任务（可以抛异常）
 * 
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年12月18日 21:22 JoeKerouac Exp $
 */
public interface Task {

    /**
     * 执行任务
     * 
     * @throws Throwable
     *             异常
     */
    void run() throws Throwable;
}
