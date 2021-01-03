package com.joe.utils.scan;

/**
 * 过滤器
 *
 * @param <T>
 *            要过滤的类型
 * @author joe
 */
public interface Filter<T> {
    /**
     * 过滤器
     *
     * @param t
     *            要过滤的组件
     * @return filter 如果通过则返回<code>true</code>
     */
    boolean filter(T t);
}
