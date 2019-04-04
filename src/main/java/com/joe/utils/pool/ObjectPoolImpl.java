package com.joe.utils.pool;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import com.joe.utils.exception.PoolObjectHolderClosedException;
import com.joe.utils.secure.impl.SignatureUtilImpl;

/**
 * Object池，从Object池中获取到的数据使用完后应该调用close关闭归还池，对象池大小上限为{@link Integer#MAX_VALUE}
 * <p>
 * 主要用于缓存一些有状态、同一时间只能一个客户端使用但是可以多次使用的对象，例如{@link SignatureUtilImpl SignatureUtilImpl}类
 *
 * @author joe
 * @version 2018.06.28 16:32
 */
public class ObjectPoolImpl<T> {
    /**
     * 池的最大大小
     */
    private final int     MAX_CAPACITY = Integer.MAX_VALUE;

    /**
     * 当前池的大小，包含未使用和已使用的总共大小
     */
    private AtomicInteger size;

    /**
     *
     */
    private Deque<T>      pool;
    private Supplier<T>   function;

    /**
     * Object池
     *
     * @param function 创建新Object元素的函数，池中的元素将使用该函数创建
     */
    public ObjectPoolImpl(Supplier<T> function) {
        this.pool = new LinkedBlockingDeque<>();
        this.function = function;
    }

    /**
     * 获取一个池元素，如果没有则会创建
     *
     * @return 池元素
     */
    public PooledObject<T> get() {
        T data = pool.pollLast();
        if (data == null) {
            data = function.get();
        }

        return new PooledObjectImpl<>(data, pool);
    }

    /**
     * 获取当前池的size，该size随时会变
     *
     * @return 当前池的size（空闲元素的size）
     */
    public int freeSize() {
        return pool.size();
    }

    /**
     * 清理当前池中的指定个数元素
     *
     * @param size 大小
     */
    public void clear(int size) {
        if (size <= 0) {
            return;
        }
        while (size-- > 0) {
            pool.pollLast();
        }
    }

    /**
     * 清理当前池中所有元素
     */
    public void clear() {
        clear(freeSize());
    }

    /**
     * 池元素持有者，用户使用完后应该调用close方法归还
     *
     * @param <T> 池元素持有的实际数据类型
     */
    public static class PooledObjectImpl<T> implements PooledObject<T> {
        private final Object lock = new Object();
        private T            data;
        private Deque<T>     pool;
        private boolean      closed;

        private PooledObjectImpl(T data, Deque<T> pool) {
            this.closed = false;
            this.data = data;
            this.pool = pool;
        }

        /**
         * 获取持有的数据
         *
         * @return 持有的数据
         */
        @Override
        public T get() {
            if (closed) {
                throw new PoolObjectHolderClosedException("PooledObject has bean closed");
            }
            synchronized (lock) {
                if (closed) {
                    throw new PoolObjectHolderClosedException("PooledObject has bean closed");
                }
                return data;
            }
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            synchronized (lock) {
                if (closed) {
                    return;
                } else {
                    pool.addLast(data);
                    this.data = null;
                    this.closed = true;
                }
            }
        }

        @Override
        protected void finalize() {
            this.close();
        }
    }
}
