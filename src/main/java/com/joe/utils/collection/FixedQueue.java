package com.joe.utils.collection;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 定长队列（长度固定，当队列长度超过指定长度时自动删除最开始添加的） 线程安全
 * 
 * @author joe
 *
 */
public class FixedQueue<E> extends ArrayBlockingQueue<E> implements Queue<E> {
    private static final long serialVersionUID = 159601021495564389L;
    private final Object lock = new Object();
    // 队列的最大长度
    private int maxSize;

    /**
     * 构建默认长度为500的队列
     */
    public FixedQueue() {
        this(500);
    }

    /**
     * 构建指定大小的队列
     * 
     * @param capacity
     *            队列的大小（队列长度的最大值为该值）
     */
    public FixedQueue(int capacity) {
        super(capacity);
        if (capacity <= 0) {
            capacity = 500;
        }
        this.maxSize = capacity;
    }

    @Override
    public boolean add(E e) {
        synchronized (lock) {
            // 如果长度超过
            if (this.size() > this.maxSize) {
                this.poll();
            }
            return super.add(e);
        }
    }

    @Override
    public boolean offer(E e) {
        synchronized (lock) {
            // 如果长度超过
            if (this.size() > this.maxSize) {
                this.poll();
            }
            return super.offer(e);
        }
    }

    @Override
    public void put(E e) throws InterruptedException {
        synchronized (lock) {
            // 如果长度超过
            if (this.size() > this.maxSize) {
                this.poll();
            }
            super.put(e);
        }
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        synchronized (lock) {
            // 如果长度超过
            if (this.size() > this.maxSize) {
                this.poll();
            }
            return super.offer(e, timeout, unit);
        }
    }
}
