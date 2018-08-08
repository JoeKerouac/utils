package com.joe.utils.concurrent;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.joe.utils.collection.LRUCacheMap;

/**
 * 锁服务，为全局业务提供锁服务，该类所有方法都是线程安全的（大量获取锁时性能会下降）
 * 
 * @author joe
 *
 */
public class LockService {
    private static final Map<String, Lock> container = new LRUCacheMap<>();

    /**
     * 根据锁名字获取指定锁，如果不存在则创建
     * 
     * @param key
     *            锁的名字
     * @return 对应的锁，如果没有则创建
     */
    public static Lock getLock(String key) {
        Lock lock = container.get(key);
        if (lock == null) {
            synchronized (container) {
                lock = container.computeIfAbsent(key, k -> new ReentrantLock());
            }
        }
        return lock;
    }

    /**
     * 获取指定的锁并锁上（该锁根据传入的key值确定，同一时间使用相同的key值调用该方法将会有一个被阻塞直至锁被释放）
     * 
     * @param key 锁的名字
     */
    public static void lock(String key) {
        getLock(key).lock();
    }

    /**
     * 获取指定的锁并尝试锁上（该锁根据传入的key值确定，同一时间使用相同的key值调用该方法将会有一个被阻塞直至锁被释放）
     *
     * @param key 锁的名字
     * @param wait 等待时长，单位为毫秒
     */
    public static boolean tryLock(String key, int wait) throws InterruptedException {
        return getLock(key).tryLock(wait, TimeUnit.MILLISECONDS);
    }

    /**
     * 根据锁名字解锁
     * 
     * @param key 锁名
     */
    public static void unlock(String key) {
        container.get(key).unlock();
    }
}
