package com.joe.utils.log;

import java.util.List;

import org.slf4j.Logger;

import com.joe.utils.common.Assert;
import com.joe.utils.common.string.StringFormater;
import com.joe.utils.log.exception.NoSupportLoggerException;
import com.joe.utils.reflect.JavaType;
import com.joe.utils.reflect.JavaTypeUtil;
import com.joe.utils.reflect.ReflectUtil;

/**
 * slf4j日志适配器
 * 
 * @author JoeKerouac
 * @version 2019年09月19日 17:45
 */
public abstract class Slf4jOperate<LEVEL> implements LogOperate<Logger, LEVEL> {

    /**
     * 等级泛型
     */
    private Class<LEVEL> levelClass;

    @SuppressWarnings("unchecked")
    public Slf4jOperate() {
        List<JavaType> javaTypes = JavaTypeUtil.getGenericSuperclasses(getClass());
        levelClass = (Class<LEVEL>) JavaTypeUtil.getRealType(javaTypes.get(0));
    }

    @Override
    public String getName(Logger logger) {
        Assert.notNull(logger, "logger不能为null");
        return logger.getName();
    }

    @Override
    public LogLevel convertToSystem(LEVEL level) {
        Assert.notNull(level, "level不能为null");
        try {
            return LogLevel.valueOf(level.toString());
        } catch (IllegalArgumentException e) {
            throw new NoSupportLoggerException("不支持的日志级别：{0}", level);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public LEVEL convert(LogLevel level) {
        Assert.notNull(level, "level不能为null");
        Object logLevel = ReflectUtil.getField(levelClass, level.toString());
        if (logLevel == null) {
            throw new NoSupportLoggerException("不支持的日志级别：{0}", level);
        }
        return (LEVEL) logLevel;
    }
}
