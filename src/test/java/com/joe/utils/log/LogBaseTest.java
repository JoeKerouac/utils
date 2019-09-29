package com.joe.utils.log;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.slf4j.LoggerFactory;

import com.joe.utils.reflect.ReflectUtil;
import com.joe.utils.reflect.clazz.ClassUtils;
import com.joe.utils.reflect.clazz.JClassLoader;

/**
 * 日志测试基础
 *
 * @author JoeKerouac
 * @version 2019年09月25日 15:10
 */
public class LogBaseTest {

    /**
     * logback的ClassLoader
     */
    private static ClassLoader LOGBACK_CLASSLOADER;

    /**
     * log4j的ClassLoader
     */
    private static ClassLoader LOG4J_CLASSLOADER;

    static {
        LOGBACK_CLASSLOADER = new JClassLoader(new String[] { ".*" }, name -> {
            try {
                URL url = findBinder(ClassUtils.getDefaultClassLoader(), name, "logback");
                if (url != null) {
                    return url.openStream();
                }
            } catch (IOException e) {
                // 忽略异常
            }
            return null;
        });

        LOG4J_CLASSLOADER = new JClassLoader(new String[] { ".*" }, name -> {
            try {
                URL url = findBinder(ClassUtils.getDefaultClassLoader(), name, "log4j");
                if (url != null) {
                    return url.openStream();
                }

                if (name.startsWith("org.slf4j")) {
                    return ClassUtils.getClassAsStream(ClassUtils.loadClass(name));
                }

            } catch (Exception e) {
                // 忽略异常
            }
            return null;
        });
    }

    /**
     * 获取一个log4j的Logger对象
     * @param loggerName loggerName
     * @return log4j的Logger对象
     */
    protected static Object getLog4jLogger(String loggerName) {
        Class<?> clazz = ClassUtils.loadClass(LoggerFactory.class.getName(), LOG4J_CLASSLOADER);
        return ReflectUtil.invoke(clazz, "getLogger", new Class[] { String.class }, loggerName);
    }

    /**
     * 获取一个logback的Logger对象
     * @param loggerName loggerName
     * @return logback的Logger对象
     */
    protected static Object getLogbackLogger(String loggerName) {
        Class<?> clazz = ClassUtils.loadClass(LoggerFactory.class.getName(), LOGBACK_CLASSLOADER);
        return ReflectUtil.invoke(clazz, "getLogger", new Class[] { String.class }, loggerName);
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
}
