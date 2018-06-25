package com.joe.utils.common;

/**
 * @author joe
 * @version 2018.06.13 14:33
 */
public class ClassUtils {

    /**
     * 获取默认classloader
     *
     * @return 当前默认classloader（先从当前线程上下文获取，获取不到获取加载该类的ClassLoader，还获取不到就获取系统classloader）
     */
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = ClassUtils.class.getClassLoader();
            if (loader == null) {
                loader = ClassLoader.getSystemClassLoader();
            }
        }
        return loader;
    }

    /**
     * 使用默认ClassLoader加载class
     *
     * @param className class名字
     * @param <T>       class实际类型
     * @return class
     * @throws ClassNotFoundException class不存在时返回该异常
     */
    public static <T> Class<T> loadClass(String className) throws ClassNotFoundException {
        return (Class<T>) getDefaultClassLoader().loadClass(className);
    }

    /**
     * 使用指定ClassLoader加载class
     *
     * @param className class名字
     * @param loader    加载class的ClassLoader
     * @param <T>       class实际类型
     * @return class
     * @throws ClassNotFoundException class不存在时返回该异常
     */
    public static <T> Class<T> loadClass(String className, ClassLoader loader) throws ClassNotFoundException {
        return (Class<T>) loader.loadClass(className);
    }
}
