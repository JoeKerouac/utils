package com.joe.utils.log.logback;

import java.util.List;

import com.joe.utils.log.LogLevel;
import com.joe.utils.log.Slf4jOperate;
import com.joe.utils.log.exception.NoSupportLoggerException;
import com.joe.utils.reflect.ReflectUtil;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * logback适配器，使用该类前请确保系统中已经引入了logback的依赖，并且使用slf4j作为接口层
 *
 * @author JoeKerouac
 * @version 2019年09月18日 14:59
 */
public class LogbackOperate extends Slf4jOperate {

    @Override
    public void setLevel(Object logger, LogLevel level) {
        if (isSameName(logger, Logger.class)) {
            setLevel(logger, level, Level.class);
        } else {
            throw new NoSupportLoggerException("不支持的日志对象:{0}", logger);
        }
    }

    @Override
    public List<Object> getAllLogger(ClassLoader loader) {
        Object factory = getLogFactory(loader);
        if (isSameName(factory, LoggerContext.class)) {
            return ReflectUtil.invoke(factory, "getLoggerList");
        } else {
            throw new NoSupportLoggerException("不支持的日志工厂对象:{0}", factory);
        }
    }
}
