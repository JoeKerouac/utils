package com.joe.utils.function;

/**
 * 事务函数
 *
 * @author joe
 * @version 2018.04.23 11:28
 */
public interface TransactionFunction<T, R> {
    /**
     * 执行事务函数
     *
     * @param t 函数参数，执行时会传入
     * @return 执行结果，如果事务执行失败应该抛出一个异常，否则认为执行成功
     */
    R invoke(T t);
}
