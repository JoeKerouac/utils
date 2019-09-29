package com.joe.utils.log;

import java.util.List;

import com.joe.utils.log.exception.NoSupportLoggerException;
import com.joe.utils.reflect.clazz.ClassUtils;

/**
 * 日志操作组件
 * 
 * @author JoeKerouac
 * @version 2019年09月18日 14:12
 */
public interface LogOperate {

    /**
     * 将指定实现日志级别转换为当前系统日志级别
     * @param level 日志级别
     * @return 当前系统日志级别
     * @throws NoSupportLoggerException 不支持时抛出该异常
     */
    LogLevel convertToSystem(Object level) throws NoSupportLoggerException;

    /**
     * 将系统日志级别转换为指定日志实现的日志级别
     * @param level 当前系统日志级别
     * @param levelClass 要转换的级别的Class
     * @param loader 当前系统日志的ClassLoader
     * @return 指定日志实现的日志级别
     * @throws NoSupportLoggerException 不支持时抛出该异常
     */
    Object convert(LogLevel level, Class<?> levelClass,
                   ClassLoader loader) throws NoSupportLoggerException;

    /**
     * 获取指定logger的日志级别
     * @param logger logger
     * @return 日志级别
     * @throws NoSupportLoggerException 不支持时抛出该异常
     */
    LogLevel getLevel(Object logger) throws NoSupportLoggerException;

    /**
     * 设置logger的日志级别
     * @param logger logger
     * @param level level
     * @throws NoSupportLoggerException 不支持时抛出该异常
     */
    void setLevel(Object logger, LogLevel level) throws NoSupportLoggerException;

    /**
     * 获取日志名
     * @param logger logger，不能为空
     * @return 日志名
     * @throws NoSupportLoggerException 不支持时抛出该异常
     */
    String getName(Object logger) throws NoSupportLoggerException;

    /**
     * 获取当前日志系统所有logger
     * 
     * @param loader 当前日志系统的ClassLoader，可以为空，为空时默认使用当前系统ClassLoader
     * @return 当前日志系统所有logger
     * @throws NoSupportLoggerException 不支持时抛出该异常
     */
    List<Object> getAllLogger(ClassLoader loader) throws NoSupportLoggerException;

    /**
     * 获取当前日志系统所有logger，默认使用当前ClassLoader
     *
     * @return 当前日志系统所有logger
     * @throws NoSupportLoggerException 不支持时抛出该异常
     */
    default List<Object> getAllLogger() throws NoSupportLoggerException {
        return getAllLogger(ClassUtils.getDefaultClassLoader());
    }

    /**
     * mdc中放置内容，相当于{@link org.slf4j.MDC#put(String, String)}
     * @param key key
     * @param value value
     */
    default void mdcPut(String key, String value) {
        mdcPut(ClassUtils.getDefaultClassLoader(), key, value);
    }

    /**
     * mdc中放置内容，相当于{@link org.slf4j.MDC#put(String, String)}，只不过会使用指定ClassLoader加载的MDC对象
     * @param key key
     * @param value value
     */
    void mdcPut(ClassLoader loader, String key, String value);
}
