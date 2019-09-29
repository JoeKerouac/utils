package com.joe.utils.log;

import org.slf4j.LoggerFactory;

import com.joe.utils.common.Assert;
import com.joe.utils.log.exception.NoSupportLoggerException;
import com.joe.utils.reflect.ClassUtils;
import com.joe.utils.reflect.ReflectUtil;
import org.slf4j.MDC;

/**
 * slf4j日志适配器(可用于logback和log4j实现)
 *
 * @author JoeKerouac
 * @version 2019年09月19日 17:45
 */
public abstract class Slf4jOperate implements LogOperate {

    @Override
    public LogLevel getLevel(Object logger) throws NoSupportLoggerException {
        Assert.notNull(logger, "logger不能为null");
        Object level;
        try {
            level = ReflectUtil.invoke(logger, "getLevel");
        } catch (Exception e) {
            throw new NoSupportLoggerException("不支持获取级别的日志对象[{0}:{1}]", logger.getClass(), logger);
        }
        return convertToSystem(level);
    }

    @Override
    public String getName(Object logger) {
        Assert.notNull(logger, "logger不能为null");
        return ReflectUtil.invoke(logger, "getName");
    }

    @Override
    public LogLevel convertToSystem(Object level) {
        Assert.notNull(level, "level不能为null");
        try {
            return LogLevel.valueOf(level.toString());
        } catch (IllegalArgumentException e) {
            throw new NoSupportLoggerException("不支持的日志级别：[{0}:{1}]", level.getClass(), level);
        }
    }

    @Override
    public Object convert(LogLevel level, Class<?> levelClass, ClassLoader loader) {
        Assert.notNull(level, "level不能为null");
        levelClass = loader == null ? levelClass : ClassUtils.reloadClass(levelClass, loader);
        Object logLevel = ReflectUtil.getFieldValue(levelClass, level.toString());
        if (logLevel == null) {
            throw new NoSupportLoggerException("不支持的日志级别：{0}:{1}", level.getClass(), level);
        }
        return logLevel;
    }

    @Override
    public void mdcPut(ClassLoader loader, String key, String value) {
            Assert.notNull(loader, "loader不能为空");
        Class<?> mdcClass = ClassUtils.reloadClass(MDC.class, loader);
        ReflectUtil.invoke(mdcClass, "put", new Class[]{String.class, String.class}, key, value);
    }

    /**
     * 通用setLevel方法
     * @param logger logger
     * @param level 级别
     * @param levelClass 当前日志实现的级别Class
     * @throws NoSupportLoggerException 不支持的日志
     */
    protected void setLevel(Object logger, LogLevel level,
                            Class<?> levelClass) throws NoSupportLoggerException {
        Assert.notNull(logger, "logger不能为null");
        Assert.notNull(level, "level不能为null");
        levelClass = ClassUtils.reloadClass(levelClass, logger.getClass().getClassLoader());
        try {
            Object descLevel = convert(level, levelClass, logger.getClass().getClassLoader());
            ReflectUtil.invoke(logger, "setLevel", new Class[] { levelClass }, descLevel);
        } catch (Exception e) {
            throw new NoSupportLoggerException("不支持设置级别的日志对象[{0}:{1}]", logger.getClass(), logger);
        }
    }

    /**
     * 获取日志工厂
     * @param loader 当前日志系统的ClassLoader，可以为空
     * @return 日志工厂
     */
    protected Object getLogFactory(ClassLoader loader) {
        loader = loader == null ? ClassUtils.getDefaultClassLoader() : loader;
        Class<?> factoryClass = ClassUtils.reloadClass(LoggerFactory.class, loader);
        return ReflectUtil.invoke(factoryClass, "getILoggerFactory");
    }

    /**
     * 指定对象的class是否和指定类型同名
     * @param obj 对象
     * @param type 类型
     * @return true表示同名
     */
    protected boolean isSameName(Object obj, Class<?> type) {
        return type.getName().equals(obj.getClass().getName());
    }
}
