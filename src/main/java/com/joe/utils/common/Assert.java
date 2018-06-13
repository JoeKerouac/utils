package com.joe.utils.common;

/**
 * @author joe
 * @version 2018.06.13 11:29
 */
public class Assert {
    /**
     * 断言参数不为null
     *
     * @param msg 为null时的异常提示
     * @param obj 参数
     */
    public static void notNull(String msg, Object obj) {
        if (obj == null) {
            throw new NullPointerException(msg);
        }
    }

    /**
     * 断言参数全不为null
     *
     * @param msg  为null时的异常提示
     * @param objs 参数列表
     */
    public static void notNull(String msg, Object... objs) {
        if (objs == null) {
            throw new NullPointerException(msg);
        }
        for (Object obj : objs) {
            notNull(msg, obj);
        }
    }
}
