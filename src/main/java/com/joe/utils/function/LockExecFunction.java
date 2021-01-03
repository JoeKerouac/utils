package com.joe.utils.function;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

/**
 * 加锁执行代码
 *
 * @author joe
 * @version 2018.04.28 18:13
 */
public interface LockExecFunction {

    ReadWriteLock getLock();

    /**
     * 加读锁执行
     *
     * @param function
     *            要执行的函数
     * @param t
     *            函数参数
     * @param <T>
     *            函数参数类型
     * @param <R>
     *            函数返回类型
     * @return 函数执行结果
     */
    default <T, R> R readLockExec(Function<T, R> function, T t) {
        Lock readLock = getLock().readLock();
        readLock.lock();
        try {
            return function.apply(t);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 加写锁执行
     *
     * @param function
     *            要执行的函数
     * @param t
     *            函数参数
     * @param <T>
     *            函数参数类型
     * @param <R>
     *            函数返回类型
     * @return 函数执行结果
     */
    default <T, R> R writeLockExec(Function<T, R> function, T t) {
        Lock writeLock = getLock().writeLock();
        writeLock.lock();
        try {
            return function.apply(t);
        } finally {
            writeLock.unlock();
        }
    }
}
