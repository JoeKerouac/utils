package com.joe.utils.common;

import java.io.InputStream;

/**
 * @author joe
 * @version 2018.06.13 14:33
 */
public class ClassUtils {
    /** The package separator character: '.' */
    private static final char  PACKAGE_SEPARATOR = '.';
    /** The ".class" file suffix */
    public static final String CLASS_FILE_SUFFIX = ".class";

    /**
     * 获取指定class的class文件的输入流
     * @param clazz class
     * @return 对应的输入流
     */
    public static InputStream getClassAsStream(Class<?> clazz) {
        return clazz.getResourceAsStream(getClassFileName(clazz));
    }

    /**
     * 获取class的class文件名（不包含包名，例如：String.class）
     * @param clazz the class
     * @return .class文件名
     */
    public static String getClassFileName(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        String className = clazz.getName();
        int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        return className.substring(lastDotIndex + 1) + CLASS_FILE_SUFFIX;
    }

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
    public static <T> Class<T> loadClass(String className,
                                         ClassLoader loader) throws ClassNotFoundException {
        return (Class<T>) loader.loadClass(className);
    }
}
