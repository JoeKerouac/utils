package com.joe.utils.reflect;

import java.io.InputStream;
import java.lang.reflect.Constructor;

import com.joe.utils.common.Assert;

/**
 * Class工具类
 *
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
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> loadClass(String className) {
        return (Class<T>) loadClass(className, getDefaultClassLoader());
    }

    /**
     * 使用指定ClassLoader加载class
     *
     * @param className class名字
     * @param loader    加载class的ClassLoader
     * @param <T>       class实际类型
     * @return class
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> loadClass(String className, ClassLoader loader) {
        Assert.notNull(className, "className不能为null");
        Assert.notNull(loader, "loader不能为null");
        switch (className) {
            case "boolean":
                return (Class<T>) boolean.class;
            case "byte":
                return (Class<T>) byte.class;
            case "char":
                return (Class<T>) char.class;
            case "short":
                return (Class<T>) short.class;
            case "int":
                return (Class<T>) int.class;
            case "long":
                return (Class<T>) long.class;
            case "double":
                return (Class<T>) double.class;
            case "float":
                return (Class<T>) float.class;
            default:
                try {
                    return (Class<T>) loader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    throw new ReflectException("找不到指定class：" + className, e);
                }
        }
    }

    /**
     * 获取class实例
     * @param className class名字
     * @param <T> class类型
     * @return class的实例
     */
    public static <T> T getInstance(String className) {
        return getInstance(loadClass(className));
    }

    /**
     * 获取class实例
     * @param className class名字
     * @param loader 加载class的classloader
     * @param <T> class类型
     * @return class的实例
     */
    public static <T> T getInstance(String className, ClassLoader loader) {
        return getInstance(loadClass(className, loader));
    }

    /**
     * 获取class的实例
     * @param clazz class
     * @param <T> class类型
     * @return Class实例
     */
    public static <T> T getInstance(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getConstructor();
            ReflectUtil.allowAccess(constructor);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new ReflectException("获取类实例异常，可能是没有默认无参构造器", e);
        }
    }
}
