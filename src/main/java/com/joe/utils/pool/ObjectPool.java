package com.joe.utils.pool;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年03月05日 15:39 JoeKerouac Exp $
 */
public interface ObjectPool<T> {

    /**
     * 获取对象池中的对象
     * @return 对象池中的对象
     */
    PooledObject<T> get();
}
