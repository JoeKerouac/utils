package com.joe.utils.reflect.clazz;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Optional;

import com.joe.utils.collection.CollectionUtil;
import com.joe.utils.common.Assert;
import com.joe.utils.reflect.ReflectException;
import com.joe.utils.reflect.ReflectUtil;

/**
 * Class工具类
 *
 * @author joe
 * @version 2018.06.13 14:33
 */
public class ClassUtils {
    /** The package separator character: '.' */
    private static final char PACKAGE_SEPARATOR = '.';
    /** The ".class" file suffix */
    public static final String CLASS_FILE_SUFFIX = ".class";

    /**
     * 获取指定类的路径
     * 
     * @param cls
     *            类
     * @return 该类的路径，获取失败返回null
     */
    public static String where(final Class<?> cls) {
        if (cls == null)
            throw new IllegalArgumentException("null input: cls");
        URL result = null;
        final String clsAsResource = cls.getName().replace('.', '/').concat(".class");
        final ProtectionDomain pd = cls.getProtectionDomain();
        if (pd != null) {
            final CodeSource cs = pd.getCodeSource();
            if (cs != null)
                result = cs.getLocation();
            if (result != null) {
                if ("file".equals(result.getProtocol())) {
                    try {
                        if (result.toExternalForm().endsWith(".jar") || result.toExternalForm().endsWith(".zip"))
                            result = new URL("jar:".concat(result.toExternalForm()).concat("!/").concat(clsAsResource));
                        else if (new File(result.getFile()).isDirectory())
                            result = new URL(result, clsAsResource);
                    } catch (MalformedURLException ignore) {
                    }
                }
            }
        }
        if (result == null) {
            final ClassLoader clsLoader = cls.getClassLoader();
            result =
                clsLoader != null ? clsLoader.getResource(clsAsResource) : ClassLoader.getSystemResource(clsAsResource);
        }
        return Optional.ofNullable(result).map(URL::getPath).orElse(null);
    }

    /**
     * 获取指定class的class文件的输入流
     * 
     * @param clazz
     *            class
     * @return 对应的输入流
     */
    public static InputStream getClassAsStream(Class<?> clazz) {
        Assert.notNull(clazz, "class不能为空");
        return clazz.getResourceAsStream(getClassFileName(clazz));
    }

    /**
     * 获取class的class文件名（不包含包名，例如：String.class）
     * 
     * @param clazz
     *            the class
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
     * 使用给定的ClassLoader重新加载class
     *
     * @param clazz
     *            class
     * @param loader
     *            重加加载class的ClassLoader
     * @param <T>
     *            class实际类型
     * @return class
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> reloadClass(Class<T> clazz, ClassLoader loader) {
        Assert.notNull(clazz, "class不能为空");
        return (Class<T>)loadClass(clazz.getName(), loader);
    }

    /**
     * 使用默认ClassLoader加载class
     *
     * @param className
     *            class名字
     * @param <T>
     *            class实际类型
     * @return class
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> loadClass(String className) {
        return (Class<T>)loadClass(className, getDefaultClassLoader());
    }

    /**
     * 使用指定ClassLoader加载class
     *
     * @param className
     *            class名字
     * @param loader
     *            加载class的ClassLoader
     * @param <T>
     *            class实际类型
     * @return class
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> loadClass(String className, ClassLoader loader) {
        Assert.notNull(className, "className不能为null");
        Assert.notNull(loader, "loader不能为null");
        switch (className) {
            case "boolean":
                return (Class<T>)boolean.class;
            case "byte":
                return (Class<T>)byte.class;
            case "char":
                return (Class<T>)char.class;
            case "short":
                return (Class<T>)short.class;
            case "int":
                return (Class<T>)int.class;
            case "long":
                return (Class<T>)long.class;
            case "double":
                return (Class<T>)double.class;
            case "float":
                return (Class<T>)float.class;
            default:
                try {
                    return (Class<T>)loader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    throw new ReflectException("找不到指定class：" + className, e);
                }
        }
    }

    /**
     * 获取class实例
     * 
     * @param className
     *            class名字
     * @param <T>
     *            class类型
     * @return class的实例
     */
    public static <T> T getInstance(String className) {
        return getInstance(loadClass(className));
    }

    /**
     * 获取class实例
     * 
     * @param className
     *            class名字
     * @param loader
     *            加载class的classloader
     * @param <T>
     *            class类型
     * @return class的实例
     */
    public static <T> T getInstance(String className, ClassLoader loader) {
        return getInstance(loadClass(className, loader));
    }

    /**
     * 获取class的实例
     * 
     * @param clazz
     *            class
     * @param <T>
     *            class类型
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

    /**
     * 获取class的实例
     * 
     * @param clazz
     *            class
     * @param paramTypes
     *            构造器参数类型
     * @param params
     *            参数
     * @param <T>
     *            class类型
     * @return Class实例
     */
    public static <T> T getInstance(Class<T> clazz, Class<?>[] paramTypes, Object[] params) {
        Assert.isTrue(CollectionUtil.safeSizeOf(paramTypes) == CollectionUtil.safeSizeOf(params));
        try {
            Constructor<T> constructor = clazz.getConstructor(paramTypes);
            ReflectUtil.allowAccess(constructor);
            return constructor.newInstance(params);
        } catch (Exception e) {
            throw new ReflectException("获取类实例异常，可能是没有默认无参构造器", e);
        }
    }
}
