package com.joe.utils.log;

import org.slf4j.Logger;

import com.joe.utils.common.Assert;
import com.joe.utils.log.exception.NoSupportLoggerException;
import com.joe.utils.reflect.ClassUtils;

import lombok.Data;

/**
 * LoggerOperate工厂
 * 
 * @author JoeKerouac
 * @version 2019年09月18日 14:00
 */
@Data
public class LoggerOperateFactory {

    /**
     * 日志对象
     */
    private final Logger logger;

    /**
     * 根据logger获取LogOperate
     * @param logger logger，不能为空
     * @return 对应的LogOperate
     * @throws NoSupportLoggerException 不支持时抛出该异常
     */
    public static LogOperate<?, ?> getConverter(Object logger) {
        Assert.notNull(logger, "logger对象不能为空");
        LogImplemention implemention;

        if (logger.getClass().getName().startsWith("ch.qos.logback")) {
            // logback的日志
            implemention = LogImplemention.LOGBACK;
        } else if (logger.getClass().getName().startsWith("org.apache.logging.log4j")) {
            // log4j的日志（slf4j门面）
            implemention = LogImplemention.LOG4J;
        } else {
            throw new NoSupportLoggerException("不支持的logger[{0}]", logger);
        }

        return getConverter(implemention);
    }

    /**
     * 获取指定LogImplemention对应的LogOperate
     * @param implemention implemention
     * @return LogOperate
     */
    public static LogOperate<?, ?> getConverter(LogImplemention implemention) {
        Assert.notNull(implemention, "implemention对象不能为空");
        return ClassUtils.getInstance(implemention.getConverterClass());
    }
}
