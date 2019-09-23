package com.joe.utils.log.log4j2;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.simple.SimpleLogger;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.spi.ExtendedLoggerWrapper;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.slf4j.Log4jLoggerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.joe.utils.log.LogLevel;
import com.joe.utils.log.Slf4jOperate;
import com.joe.utils.log.exception.LogException;
import com.joe.utils.log.exception.NoSupportLoggerException;
import com.joe.utils.reflect.ReflectUtil;

/**
 * log4j适配器，使用该类前请确保系统中已经引入了log4j的依赖，并且使用slf4j作为接口层
 *
 * @author JoeKerouac
 * @version 2019年09月18日 14:25
 */
public class Log4jOperate extends Slf4jOperate<Level> {

    @Override
    public LogLevel getLevel(Logger logger) {
        AbstractLogger abstractLogger = check(logger);
        Level level = abstractLogger.getLevel();
        return convertToSystem(level);
    }

    @Override
    public void setLevel(Logger logger, LogLevel level) {
        AbstractLogger abstractLogger = check(logger);
        if (abstractLogger instanceof org.apache.logging.log4j.core.Logger
            || abstractLogger instanceof SimpleLogger || abstractLogger instanceof StatusLogger) {
            ReflectUtil.invoke(logger, "setLevel", new Class[] { Level.class }, convert(level));
        } else if (abstractLogger instanceof ExtendedLoggerWrapper) {
            setLevel(ReflectUtil.getFieldValue(logger, "logger"), level);
        } else {
            throw new NoSupportLoggerException("不支持的日志对象[{0}]", logger);
        }
    }

    @Override
    public List<Logger> getAllLogger() {
        ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
        if (!(iLoggerFactory instanceof Log4jLoggerFactory)) {
            throw new NoSupportLoggerException("不支持的日志工厂：{0}", iLoggerFactory);
        }
        Log4jLoggerFactory log4jLoggerFactory = (Log4jLoggerFactory) iLoggerFactory;
        Map<LoggerContext, ConcurrentMap<String, Logger>> registry = ReflectUtil
            .getFieldValue(log4jLoggerFactory, "registry");
        if (registry == null) {
            throw new NoSupportLoggerException("不支持的日志工厂类型[{0}]，请检查是否版本号有问题", log4jLoggerFactory);
        }
        return registry.values().stream().flatMap(map -> map.values().stream())
            .collect(Collectors.toList());
    }

    public AbstractLogger check(Logger logger) {
        if (!(logger instanceof AbstractLogger)) {
            throw new LogException("logger[{0}]对象不是log4j的logger对象", logger);
        }
        return (AbstractLogger) logger;
    }
}
