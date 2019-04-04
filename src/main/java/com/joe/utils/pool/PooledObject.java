package com.joe.utils.pool;

import java.util.function.Supplier;

/**
 * 池对象持有者，获取到之后可以从该对象
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年03月05日 15:02 JoeKerouac Exp $
 */
public interface PooledObject<T> extends AutoCloseable, Supplier<T> {

    /**
     * 关闭方法，禁止抛出异常
     */
    void close();
}
