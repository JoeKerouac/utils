package com.joe.utils.log;

import java.util.function.Supplier;
import java.util.logging.Level;

import com.joe.utils.common.Assert;
import com.joe.utils.common.string.StringConst;
import com.joe.utils.common.string.StringUtils;
import com.joe.utils.log.exception.LogException;
import com.joe.utils.log.exception.NoSupportLoggerException;
import com.joe.utils.reflect.ReflectException;
import com.joe.utils.reflect.ReflectUtil;

import lombok.Getter;

/**
 * 日志打印任务
 * 
 * @author JoeKerouac
 * @version 2019年09月18日 11:51
 */
@Getter
public class LogTask implements Runnable {

    /**
     * slf4j/common-logging实现日志打印函数入参
     */
    private static Class[]         DEFAULT_PARAMETER_TYPES = new Class[] { String.class,
                                                                           Object[].class };

    /**
     * java.util.logging实现日志打印函数入参
     */
    private static Class[]         JUL_PARAMETER_TYPES     = new Class[] { Level.class,
                                                                           String.class,
                                                                           Object[].class };

    /**
     * 日志logger对象
     */
    private final Object           logger;

    /**
     * 打印级别，必填
     */
    private final LogLevel         level;

    /**
     * 异常，可空
     */
    private final Throwable        throwable;

    /**
     * 日志消息供应商，起延迟加载的作用
     */
    private final Supplier<String> msgSupplier;

    /**
     * 消息参数，可选
     */
    private final Object[]         args;

    /**
     * 打印函数参数列表
     */
    private final Class[]          paramterTypes;

    public LogTask(Object logger, LogLevel level, Throwable throwable, Supplier<String> msgSupplier,
                   Object[] args) {
        this.logger = logger;
        this.level = level;
        this.throwable = throwable;
        this.msgSupplier = msgSupplier;
        this.args = args;
        this.paramterTypes = checkLogger(logger, level);
    }

    @Override
    public void run() {
        String msg = getMsgTemp();
        try {
            if (paramterTypes == JUL_PARAMETER_TYPES) {
                // jul的日志打印
                ReflectUtil.invoke(logger, "log", paramterTypes, toJul(level), msg, args);
            } else {
                if (level == LogLevel.OFF) {
                    return;
                }
                LogLevel l = level == LogLevel.ALL ? LogLevel.INFO : level;
                ReflectUtil.invoke(logger, l.getName(), paramterTypes, msg, args);
            }
        } catch (Exception e) {
            // 忽略该异常
        }
    }

    /**
     * 将日志级别转换为jul的日志级别
     * @param level 级别
     * @return jul的日志级别
     */
    private Level toJul(LogLevel level) {
        switch (level) {
            case DEBUG:
                return Level.CONFIG;
            case INFO:
                return Level.INFO;
            case WARN:
                return Level.WARNING;
            case ERROR:
                return Level.SEVERE;
            case ALL:
                return Level.ALL;
            case OFF:
                return Level.OFF;
            default:
                return Level.OFF;
        }
    }

    /**
     * 获取日志消息模板
     * @return 消息模板
     */
    private String getMsgTemp() {
        String msgTemp = StringConst.EMPTY;

        if (msgSupplier != null) {
            msgTemp = msgSupplier.get();
            msgTemp = msgTemp == null ? StringConst.EMPTY : msgTemp;
        }

        if (throwable != null) {
            if (StringUtils.isNotEmpty(msgTemp)) {
                msgTemp += StringConst.LINE_BREAK;
            }
            msgTemp += throwable.toString();
        }

        return msgTemp;
    }

    /**
     * 校验logger对象
     * @param logger logger对象
     * @param level 日志级别
     * @return 日志打印函数入参类型列表
     */
    private Class[] checkLogger(Object logger, LogLevel level) {
        Assert.notNull(logger, "日志对象不能为null");
        try {
            // 首先尝试slf4j/common-logging
            ReflectUtil.getMethod(logger.getClass(), level.getName(), DEFAULT_PARAMETER_TYPES);
            return DEFAULT_PARAMETER_TYPES;
        } catch (ReflectException e) {
            try {
                // 继续尝试java.util.logging
                ReflectUtil.getMethod(logger.getClass(), "log", JUL_PARAMETER_TYPES);
                return JUL_PARAMETER_TYPES;
            } catch (ReflectException e1) {
                throw new NoSupportLoggerException("不支持的日志类型：{0}", logger.getClass());
            }
        }
    }
}
