package com.joe.utils.function;

/**
 * 获取数据函数
 *
 * @author joe
 * @version 2018.06.28 16:44
 */
public interface GetObjectFunction<T> {
    /**
     * 获取一个数据
     *
     * @return 数据，不能为空
     */
    T get();
}
