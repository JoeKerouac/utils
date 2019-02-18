package com.joe.utils.function;

/**
 * 原生数组生成器
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年02月18日 15:00 JoeKerouac Exp $
 */
public interface ArrayCreater<T> {

    /**
     * 创建一个指定类型指定长度的原生数组
     * @param length 指定长度
     * @return 指定长度的原生数组
     */
    T[] create(int length);
}
