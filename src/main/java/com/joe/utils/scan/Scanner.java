package com.joe.utils.scan;

import java.util.List;

/**
 * 扫描器
 *
 * @param <T> 要扫描的类型
 * @param <F> filter类型
 * @author joe
 */
public interface Scanner<T, F> {
    /**
     * 扫描所有
     *
     * @param args 参数
     * @return 扫描结果
     */
    List<T> scan(Object... args);

    /**
     * 扫描所有并且用Filter过滤
     *
     * @param excludeFilters 过滤器，返回true时子项将被过滤删除
     * @param args    参数
     * @return 扫描结果
     */
    List<T> scanByFilter(List<F> excludeFilters, Object... args);
}
