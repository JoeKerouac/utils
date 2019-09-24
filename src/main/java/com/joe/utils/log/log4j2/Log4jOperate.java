package com.joe.utils.log.log4j2;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.simple.SimpleLogger;
import org.apache.logging.log4j.spi.ExtendedLoggerWrapper;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.slf4j.Log4jLogger;
import org.apache.logging.slf4j.Log4jLoggerFactory;

import com.joe.utils.log.LogLevel;
import com.joe.utils.log.Slf4jOperate;
import com.joe.utils.log.exception.NoSupportLoggerException;
import com.joe.utils.reflect.ReflectUtil;

/**
 * log4j适配器，使用该类前请确保系统中已经引入了log4j的依赖，并且使用slf4j作为接口层
 *
 * @author JoeKerouac
 * @version 2019年09月18日 14:25
 */
public class Log4jOperate extends Slf4jOperate {

    @Override
    public void setLevel(Object logger, LogLevel level) {
        if (isSameName(logger, org.apache.logging.log4j.core.Logger.class)
            || isSameName(logger, SimpleLogger.class) || isSameName(logger, StatusLogger.class)) {
            setLevel(logger, level, Level.class);
        } else if (isSameName(logger, ExtendedLoggerWrapper.class)
                   || isSameName(logger, Log4jLogger.class)) {
            setLevel(ReflectUtil.getFieldValue(logger, "logger"), level);
        } else {
            throw new NoSupportLoggerException("不支持的日志对象[{0}]", logger);
        }
    }

    @Override
    public LogLevel getLevel(Object logger) throws NoSupportLoggerException {
        if (isSameName(logger, org.apache.logging.log4j.core.Logger.class)
            || isSameName(logger, SimpleLogger.class) || isSameName(logger, StatusLogger.class)) {
            return super.getLevel(logger);
        } else if (isSameName(logger, ExtendedLoggerWrapper.class)
                   || isSameName(logger, Log4jLogger.class)) {
            return getLevel(ReflectUtil.getFieldValue(logger, "logger"));
        } else {
            throw new NoSupportLoggerException("不支持的日志对象[{0}]", logger);
        }
    }

    @Override
    public List<Object> getAllLogger(ClassLoader loader) {
        Object iLoggerFactory = getLogFactory(loader);
        if (!isSameName(iLoggerFactory, Log4jLoggerFactory.class)) {
            throw new NoSupportLoggerException("不支持的日志工厂：{0}", iLoggerFactory);
        }
        Map<LoggerContext, ConcurrentMap<String, Object>> registry = ReflectUtil
            .getFieldValue(iLoggerFactory, "registry");
        if (registry == null) {
            throw new NoSupportLoggerException("不支持的日志工厂类型[{0}]，请检查是否版本号有问题", iLoggerFactory);
        }
        return registry.values().stream().flatMap(map -> map.values().stream())
            .collect(Collectors.toList());
    }
}
