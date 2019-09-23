package com.joe.utils.log.logback;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import com.joe.utils.log.LogLevel;
import com.joe.utils.log.Slf4jOperate;
import com.joe.utils.log.exception.NoSupportLoggerException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * logback适配器，使用该类前请确保系统中已经引入了logback的依赖，并且使用slf4j作为接口层
 *
 * @author JoeKerouac
 * @version 2019年09月18日 14:59
 */
public class LogbackOperate extends Slf4jOperate<Level> {

    /**
     * 类加载的时候就获取LoggerContext
     */
    private final static LoggerContext CONTEXT;

    static {
        ILoggerFactory factory = LoggerFactory.getILoggerFactory();
        if (factory instanceof LoggerContext) {
            CONTEXT = (LoggerContext) factory;
        } else {
            CONTEXT = null;
        }
    }

    @Override
    public LogLevel getLevel(org.slf4j.Logger logger) {
        if (logger instanceof Logger) {
            return convertToSystem(((Logger) logger).getLevel());
        } else {
            throw new NoSupportLoggerException("不支持的日志对象:{0}", logger);
        }
    }

    @Override
    public void setLevel(org.slf4j.Logger logger, LogLevel level) {
        if (logger instanceof Logger) {
            ((Logger) logger).setLevel(convert(level));
        } else {
            throw new NoSupportLoggerException("不支持的日志对象:{0}", logger);
        }
    }

    @Override
    public List<org.slf4j.Logger> getAllLogger() {
        return new ArrayList<>(CONTEXT.getLoggerList());
    }
}
