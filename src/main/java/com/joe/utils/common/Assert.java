package com.joe.utils.common;

/**
 * @author joe
 * @version 2018.06.13 11:29
 */
public class Assert {

    /**
     * 断言给定时间是一个正确的时间段（即开始时间小于结束时间）
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @param format 时间格式
     */
    public static void isTimeZone(String beginTime, String endTime, String format) {
        DateUtil.parse(beginTime, format);
        DateUtil.parse(endTime, format);
        if (beginTime.compareTo(endTime) > 0) {
            throw new IllegalArgumentException("开始时间必须小于结束时间");
        }
    }

    /**
     * 断言参数true
     *
     * @param flag 参数
     */
    public static void isTrue(boolean flag) {
        isTrue(flag, null);
    }

    /**
     * 断言参数true
     *
     * @param flag 参数
     * @param msg 为false时的异常提示
     */
    public static void isTrue(boolean flag, String msg) {
        if (!flag) {
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * 断言参数为false
     *
     * @param flag 参数
     */
    public static void isFalse(boolean flag) {
        isFalse(flag, null);
    }

    /**
     * 断言参数为false
     *
     * @param flag 参数
     * @param msg 为true时的异常提示
     */
    public static void isFalse(boolean flag, String msg) {
        if (flag) {
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * 断言参数不为null
     *
     * @param obj 参数
     */
    public static void notNull(Object obj) {
        notNull(obj, null);
    }

    /**
     * 断言参数不为null
     *
     * @param obj 参数
     * @param msg 为null时的异常提示
     */
    public static void notNull(Object obj, String msg) {
        if (obj == null) {
            throw new NullPointerException(msg);
        }
    }

    /**
     * 断言参数全不为null
     *
     * @param objs 参数列表
     */
    public static void notNull(Object[] objs) {
        notNull(objs, null);
    }

    /**
     * 断言参数全不为null
     *
     * @param objs 参数列表
     * @param msg  为null时的异常提示
     */
    public static void notNull(Object[] objs, String msg) {
        if (objs == null) {
            throw new NullPointerException(msg);
        }
        for (Object obj : objs) {
            notNull(obj, msg);
        }
    }
}
