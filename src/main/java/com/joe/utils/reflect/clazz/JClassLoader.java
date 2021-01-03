package com.joe.utils.reflect.clazz;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * 自定义ClassLoader，可以突破双亲委托机制（对于java.lang.*不能突破），优先使用{@link #classProvider}提供的类数据定义类
 *
 * @author JoeKerouac
 * @version 2019年09月25日 10:51
 */
public class JClassLoader extends URLClassLoader {

    /**
     * 要突破双亲委托的包集合，支持正则，如果加载不到才会去父类加载
     */
    private final String[] packages;

    /**
     * 父ClassLoader
     */
    private final ClassLoader parent;

    /**
     * class数据供应商，如果该class是需要本加载器自己加载的那么将会优先使用本供应商获取class数据
     */
    private final ClassProvider classProvider;

    /**
     * resource供应商，供方法{@link #getResources(String)}使用
     */
    private final Function<String, Enumeration<URL>> resourceProvider;

    public JClassLoader(String[] packages) {
        this(null, packages);
    }

    public JClassLoader(URL[] urls, String[] packages) {
        this(urls, packages, null);
    }

    public JClassLoader(String[] packages, ClassProvider classProvider) {
        this(null, packages, classProvider);
    }

    public JClassLoader(URL[] urls, String[] packages, ClassProvider classProvider) {
        this(urls, packages, null, classProvider);
    }

    public JClassLoader(URL[] urls, String[] packages, ClassLoader parent, ClassProvider classProvider) {
        this(urls, packages, parent, classProvider, null);
    }

    public JClassLoader(URL[] urls, String[] packages, ClassProvider classProvider,
        Function<String, Enumeration<URL>> resourceProvider) {
        this(urls, packages, null, classProvider, resourceProvider);
    }

    public JClassLoader(URL[] urls, String[] packages, ClassLoader parent, ClassProvider classProvider,
        Function<String, Enumeration<URL>> resourceProvider) {
        super(urls == null ? new URL[0] : urls);
        this.packages = packages == null ? new String[0] : packages;
        this.parent = parent == null ? JClassLoader.class.getClassLoader() : parent;
        this.classProvider = classProvider != null ? classProvider : name -> {
            Class<?> clazz;
            try {
                clazz = ClassUtils.loadClass(name, parent != null ? parent : JClassLoader.class.getClassLoader());
            } catch (Exception e) {
                throw new ClassNotFoundException(name);
            }
            return ClassUtils.getClassAsStream(clazz);
        };
        this.resourceProvider = resourceProvider != null ? resourceProvider : name -> new JEnumeration<>();
    }

    /**
     * 定义Class
     * 
     * @param name
     *            class名
     * @param data
     *            class数据
     * @return class
     */
    public Class<?> defineClass(String name, byte[] data) {
        return super.defineClass(name, data, 0, data.length);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> clazz = findLoadedClass(name);

            if (clazz == null && needLoadBySelf(name)) {
                // 优先使用用户提供的provider加载类数据
                InputStream stream;
                try {
                    stream = classProvider.findClassStream(name);
                } catch (ClassNotFoundException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ClassNotFoundException(String.format("class数据读取异常，加载类[%s]失败", name), e);
                }
                if (stream != null) {
                    byte[] classData;
                    try {
                        classData = read(stream);
                    } catch (IOException e) {
                        // 对于流读取失败的要抛出异常，不能再使用父类加载器加载
                        throw new ClassNotFoundException(String.format("class数据读取异常，加载类[%s]失败", name), e);
                    }

                    // 定义该类
                    clazz = defineClass(name, classData);
                }
            }

            // 如果还是null就调用父加载器来加载
            if (clazz == null) {
                super.loadClass(name, resolve);
            }

            if (resolve) {
                resolveClass(clazz);
            }
        }

        return super.loadClass(name, resolve);
    }

    @Override
    public URL getResource(String name) {
        Enumeration<URL> enumeration = resourceProvider.apply(name);
        if (enumeration != null && enumeration.hasMoreElements()) {
            return enumeration.nextElement();
        }
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> enumeration = resourceProvider.apply(name);

        if (enumeration == null) {
            return super.getResources(name);
        }

        List<URL> data = read(enumeration);
        enumeration = super.getResources(name);

        if (enumeration != null) {
            data.addAll(read(enumeration));
        }

        return new JEnumeration<>(data);
    }

    /**
     * 读取Enumeration
     * 
     * @param enumeration
     *            Enumeration
     * @param <E>
     *            数据类型
     * @return 读取到的Enumeration
     */
    private <E> List<E> read(Enumeration<E> enumeration) {
        List<E> list = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            list.add(enumeration.nextElement());
        }
        return list;
    }

    /**
     * 从输入流中读取内容
     * 
     * @param stream
     *            输入流
     * @return 读取到的内容
     * @throws IOException
     *             读取异常
     */
    private byte[] read(InputStream stream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len;
        while ((len = stream.read(data)) > 0) {
            out.write(data, 0, len);
        }
        return out.toByteArray();
    }

    /**
     * 是否需要突破双亲委托自己加载，如果加载不到不会去父ClassLoader查找而是直接抛出ClassNotFoundException
     * 
     * @param name
     *            名字
     * @return true表示需要突破双亲委托自己加载
     */
    private boolean needLoadBySelf(String name) {
        // java.lang包的不能突破
        if (name.startsWith("java.lang")) {
            return false;
        }
        for (String str : packages) {
            if (str == null || str.trim().length() == 0) {
                continue;
            }
            try {
                if (name.equals(str) || name.startsWith(str) || Pattern.matches(str, name)) {
                    return true;
                }
            } catch (Exception e) {
                // 忽略异常
            }
        }
        return false;
    }
}
