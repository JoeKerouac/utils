package com.joe.utils.object;

/**
 * 自定义对象包装
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年02月18日 13:43 JoeKerouac Exp $
 */
public interface CustomObject<T> {

    /**
     * 判断给定参数列表是否包含本对象包含的实际对象
     * @param args 参数列表
     * @return 返回true表示本对象包含的实际对象包含在参数列表中
     */
    boolean in(T... args);

    static <T> CustomObject<T> of(T object) {
        return new CustomObjectImpl<>(object);
    }
}
