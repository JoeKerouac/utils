package com.joe.utils.concurrent;

import java.lang.reflect.Field;

import com.joe.utils.reflect.ReflectUtil;

/**
 * 如果当前线程ThreadLocal没有值可以配合{@link CustomThread}从父线程中取出值，前提是此时父线程仍然在运行中，如果父线程已经结束那么是无法获取的
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年04月09日 19:56 JoeKerouac Exp $
 */
public class CustomThreadLocal<T> extends ThreadLocal<T> {

    @Override
    public T get() {
        T result = super.get();

        if (result != null) {
            return result;
        }

        // 开始遍历父线程，从父线程中取
        Thread currentThread = Thread.currentThread();

        while (true) {
            // 判断当前线程是否是自定义线程
            if (currentThread instanceof CustomThread) {
                // 获取父线程，将当前线程置为父线程
                currentThread = ((CustomThread) currentThread).getParent();
                // 如果父线程为null那么直接返回
                if (currentThread == null) {
                    return null;
                }
                // 获取父线程的ThreadLocalMap
                Field field = ReflectUtil.getField(Thread.class, "threadLocals");
                Object threadLocalMap = ReflectUtil.getFieldValue(currentThread, field);
                // 父线程不存在ThreadLocalMap时继续尝试递归查找父线程
                if (threadLocalMap == null) {
                    continue;
                }

                Object entry = ReflectUtil.invoke(threadLocalMap, "getEntry",
                    new Class[] { ThreadLocal.class }, this);
                // 不存在时尝试递归查找
                if (entry == null) {
                    continue;
                }
                return ReflectUtil.getFieldValue(entry, "value");
            } else {
                return null;
            }
        }
    }
}
