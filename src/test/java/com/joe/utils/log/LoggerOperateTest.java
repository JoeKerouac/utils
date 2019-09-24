package com.joe.utils.log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.joe.utils.common.IOUtils;
import com.joe.utils.reflect.ClassUtils;
import com.joe.utils.reflect.ReflectUtil;

/**
 * 日志操作测试
 *
 * @author JoeKerouac
 * @version 2019年09月19日 20:03
 */
public class LoggerOperateTest {

    private static final String LOGGER_NAME = "com.joe";

    @Test
    public void testLoggerOperate() {
        // logback的ClassLoader
        LogClassLoader logbackClassLoader = new LogClassLoader("logback");
        // log4j的ClassLoader
        LogClassLoader log4jClassLoader = new LogClassLoader("log4j");

        Object logbackLogger = getLogger(logbackClassLoader);
        Object log4jLogger = getLogger(log4jClassLoader);
        // 系统日志对象
        Object systemLogger = getLogger(LoggerOperateTest.class.getClassLoader());

        invokeTest(logbackLogger);
        invokeTest(log4jLogger);
        invokeTest(systemLogger);
    }

    private void invokeTest(Object logger) {
        LogOperate logOperate = LoggerOperateFactory.getConverter(logger);
        logOperate.setLevel(logger, LogLevel.ERROR);
        LogLevel level = logOperate.getLevel(logger);
        Assert.assertEquals(LogLevel.ERROR, level);
        Assert.assertEquals(LOGGER_NAME, logOperate.getName(logger));
        Assert.assertTrue(!logOperate.getAllLogger(logger.getClass().getClassLoader()).isEmpty());
    }

    /**
     * 获取指定class对应的文件URL，会根据jarName匹配，用于同一个class在多个jar中都存在，并且都被同一个ClassLoader加载，要获取指定jar中文件URL时可以使用该方法
     * @param loader ClassLoader
     * @param className class名称
     * @param jarName jar包名称（可以是部分，但必须是连续的）
     * @return 指定class在指定jar包中的url，不存在返回null
     */
    private static URL findBinder(ClassLoader loader, String className, String jarName) {
        try {
            Enumeration<URL> paths = loader
                .getResources(className.replaceAll("\\.", "/") + ".class");
            while (paths.hasMoreElements()) {
                URL path = paths.nextElement();
                if (path.getPath().contains(jarName)) {
                    return path;
                }
            }
            return null;
        } catch (IOException ioe) {
            // 忽略异常
            return null;
        }
    }

    /**
     * 使用反射和指定ClassLoader获取该ClassLoader中的Logger
     * @param classLoader ClassLoader
     * @return Logger
     */
    private static Object getLogger(ClassLoader classLoader) {
        Class<?> clazz = ClassUtils.loadClass(LoggerFactory.class.getName(), classLoader);
        return ReflectUtil.invoke(clazz, "getLogger", new Class[] { String.class }, LOGGER_NAME);
    }

    /**
     * 自定义加载器，用于突破双亲委托
     */
    static class LogClassLoader extends URLClassLoader {
        private String      logName;

        private ClassLoader parent;

        public LogClassLoader(String logName) {
            super(new URL[] {}, LogClassLoader.class.getClassLoader());
            this.logName = logName;
            this.parent = LogClassLoader.class.getClassLoader();
        }

        @Override
        public URL getResource(String name) {
            if (name.equals("META-INF/log4j-provider.properties")) {
                return parent.getResource(name);
            } else {
                return super.getResource(name);
            }
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            // log4j中org.apache.logging.log4j.util.ProviderUtil.ProviderUtil的构造器会通过使用ClassLoader获取该resource来确定是否使用ClassLoader
            if (name.equals("META-INF/log4j-provider.properties")) {
                return new Enumeration<URL>() {

                    private URL url = getResource(name);

                    @Override
                    public boolean hasMoreElements() {
                        return url != null;
                    }

                    @Override
                    public URL nextElement() {
                        URL old = url;
                        url = null;
                        return old;
                    }
                };
            } else {
                return super.getResources(name);
            }
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (name.startsWith("org.slf4j.LoggerFactory")) {
                Class<?> clazz = findLoadedClass(name);
                if (clazz == null) {
                    throw new ClassNotFoundException(name);
                }
                return clazz;
            }
            return super.findClass(name);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            /*
             * 逻辑：
             * 1、首先查找已加载的类；
             * 2、如果不存在那么尝试从log4j或者logback的jar包中寻找该类文件，如果找到了那么使用该类文件定义class；
             * 3、如果没有从log4j或者logback的jar包中寻找到该类文件，那么判断类是否是org.slf4j的接口，如果是那么重新定义该class以方便重新绑定log4j/logback实现；
             * 4、如果也不是org.slf4j的接口，那么使用parent去加载该类；
             */
            Class<?> clazz = findLoadedClass(name);
            if (clazz != null) {
                return clazz;
            }

            synchronized (getClassLoadingLock(name)) {
                if ((clazz = findLoadedClass(name)) != null) {
                    return clazz;
                }

                URL url = findBinder(parent, name, logName);
                if (url != null) {
                    try {
                        return internalDefineClass(name, url.openStream(), resolve);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    // 对于org.slf4j的需要重新加载以链接新的StaticLoggerBinder
                    if (name.startsWith("org.slf4j.LoggerFactory")) {
                        try {
                            InputStream stream = ClassUtils
                                .getClassAsStream(ClassUtils.loadClass(name, parent));
                            return internalDefineClass(name, stream, resolve);
                        } catch (Exception e) {
                            throw new ClassNotFoundException(name);
                        }
                    } else {
                        return parent.loadClass(name);
                    }

                }
            }
        }

        /**
         * 根据输入流内容定义class
         * @param name className
         * @param stream 输入流
         * @param resolve 是否链接
         * @return class
         */
        private Class<?> internalDefineClass(String name, InputStream stream, boolean resolve) {
            try {
                byte[] data = IOUtils.read(stream, 1024);
                Class<?> clazz = defineClass(name, data, 0, data.length);
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
