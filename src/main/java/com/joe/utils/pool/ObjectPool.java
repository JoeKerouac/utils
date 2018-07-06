package com.joe.utils.pool;

import com.joe.utils.function.GetObjectFunction;
import com.joe.utils.secure.RSA;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Object池，从Object池中获取到的数据使用完后应该调用close关闭归还池
 * <p>
 * 主要用于缓存一些有状态、同一时间只能一个客户端使用但是可以多次使用的对象，例如{@link RSA RSA}类
 *
 * @author joe
 * @version 2018.06.28 16:32
 */
public class ObjectPool<T> {
    private Deque<PoolObjectHolder<T>> pool;
    private GetObjectFunction<T> function;

    /**
     * Object池
     *
     * @param function 创建新Object元素的函数，池中的元素将使用该函数创建
     */
    public ObjectPool(GetObjectFunction<T> function) {
        this.pool = new LinkedBlockingDeque<>();
        this.function = function;
    }

    /**
     * 获取一个池元素，如果没有则会创建
     *
     * @return 池元素
     */
    public PoolObjectHolder<T> get() {
        PoolObjectHolder<T> data = pool.pollLast();
        if (data == null) {
            data = new PoolObjectHolder<>(function.get(), this);
        }
        return data;
    }

    /**
     * 获取当前池的size，该size随时会变
     *
     * @return 当前池的size（空闲元素的size）
     */
    public int size() {
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
        clear(size());
    }

    /**
     * 池元素持有者，用户使用完后应该调用close方法归还
     *
     * @param <T> 池元素持有的实际数据类型
     */
    public static class PoolObjectHolder<T> implements AutoCloseable {
        private T data;
        private ObjectPool<T> pool;

        private PoolObjectHolder(T data, ObjectPool<T> pool) {
            this.data = data;
            this.pool = pool;
        }

        /**
         * 获取持有的数据
         *
         * @return 持有的数据
         */
        public T get() {
            return data;
        }

        @Override
        public void close() {
            pool.pool.addLast(this);
        }
    }
}
