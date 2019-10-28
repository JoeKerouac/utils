package com.joe.utils.concurrent;

import com.joe.utils.common.Assert;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;

/**
 * 并发工具
 * 
 * @author JoeKerouac
 * @version 2019年10月11日 20:24
 */
public class ConcurrentUtil {

    /**
     * 带锁执行，任务将会在锁内执行
     * @param lock 锁
     * @param task 要执行的任务
     */
    public static void execWithLock(Lock lock, Runnable task) {
        Assert.notNull(lock);
        Assert.notNull(task);
        lock.lock();
        try {
            task.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 带锁执行，任务将会在锁内执行
     * @param lock 锁
     * @param task 要执行的任务
     * @return 执行结果
     * @throws Exception 执行中的异常
     */
    public static <T> T execWithLock(Lock lock, Callable<T> task) throws Exception {
        Assert.notNull(lock);
        Assert.notNull(task);
        lock.lock();
        try {
            return task.call();
        } finally {
            lock.unlock();
        }
    }
}
