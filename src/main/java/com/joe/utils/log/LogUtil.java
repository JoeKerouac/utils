package com.joe.utils.log;

import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.logging.Level;

import com.joe.threadx.InterceptableThreadPoolExecutor;
import com.joe.threadx.InterceptableThreadPoolExecutorFactory;
import com.joe.threadx.ThreadPoolConfig;
import com.joe.threadx.ThreadxConst;
import com.joe.threadx.interceptor.mdc.MDCTaskInterceptor;
import com.joe.utils.common.Assert;
import com.joe.utils.common.string.StringConst;
import com.joe.utils.common.string.StringUtils;
import com.joe.utils.reflect.ReflectException;
import com.joe.utils.reflect.ReflectUtil;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * 日志工具，提供日志模板延迟加载和日志异步打印的功能，同时支持slf4j
 *
 * @author JoeKerouac
 * @version 2019年09月17日 14:17
 */
public class LogUtil {

    /**
     * slf4j/common-logging实现日志打印函数入参
     */
    private static Class[]               DEFAULT_PARAMETER_TYPES = new Class[] { String.class,
                                                                                 Object[].class };

    /**
     * java.util.logging实现日志打印函数入参
     */
    private static Class[]               JUL_PARAMETER_TYPES     = new Class[] { Level.class,
                                                                                 String.class,
                                                                                 Object[].class };

    private static final ExecutorService SERVICE;

    static {
        ThreadPoolConfig config = ThreadxConst.IO_THREAD_POO_CONFIG_SUPPLIER.get();
        InterceptableThreadPoolExecutor executor = InterceptableThreadPoolExecutorFactory
            .build(config, true);
        // 支持MDC功能
        executor.addLastTaskInterceptor(new MDCTaskInterceptor());
        SERVICE = executor;
    }

    /**
     * 打印日志
     * @param logger logger，不能为空，日志对象（Logger）
     * @param level level，为空时默认debug
     * @param msg 消息
     */
    public static void log(Object logger, LogLevel level, String msg) {
        logExec(logger, level, null, () -> msg, null);
    }

    /**
     * 打印日志
     * @param logger logger，不能为空，日志对象（Logger）
     * @param level level，为空时默认debug
     * @param msg 消息
     * @param args 参数
     */
    public static void log(Object logger, LogLevel level, String msg, Object... args) {
        logExec(logger, level, null, () -> msg, args);
    }

    /**
     * 打印日志
     * @param logger logger，不能为空，日志对象（Logger）
     * @param level level，为空时默认debug
     * @param msgSupplier 消息提供商，用于延迟初始化
     */
    public static void log(Object logger, LogLevel level, Supplier<String> msgSupplier) {
        logExec(logger, level, null, msgSupplier, null);
    }

    /**
     * 打印日志
     * @param logger logger，不能为空，日志对象（Logger）
     * @param level level，为空时默认debug
     * @param msgSupplier 消息提供商，用于延迟初始化
     * @param args 参数
     */
    public static void log(Object logger, LogLevel level, Supplier<String> msgSupplier,
                           Object... args) {
        logExec(logger, level, null, msgSupplier, args);
    }

    /**
     * 打印日志
     * @param logger logger，不能为空，日志对象（Logger）
     * @param level level，为空时默认debug
     * @param throwable throwable，可以为空
     * @param msg 消息
     */
    public static void log(Object logger, LogLevel level, Throwable throwable, String msg) {
        logExec(logger, level, throwable, () -> msg, null);
    }

    /**
     * 打印日志
     * @param logger logger，不能为空，日志对象（Logger）
     * @param level level，为空时默认debug
     * @param throwable throwable，可以为空
     * @param msg 消息
     * @param args 参数
     */
    public static void log(Object logger, LogLevel level, Throwable throwable, String msg,
                           Object... args) {
        logExec(logger, level, throwable, () -> msg, args);
    }

    /**
     * 打印日志
     * @param logger logger，不能为空，日志对象（Logger）
     * @param level level，为空时默认debug
     * @param throwable throwable，可以为空
     * @param msgSupplier 消息提供商，用于延迟初始化
     */
    public static void log(Object logger, LogLevel level, Throwable throwable,
                           Supplier<String> msgSupplier) {
        logExec(logger, level, throwable, msgSupplier, null);
    }

    /**
     * 打印日志
     * @param logger logger，不能为空，日志对象（Logger）
     * @param level level，为空时默认debug
     * @param throwable throwable，可以为空
     * @param msgSupplier 消息提供商，用于延迟初始化
     * @param args 参数
     */
    public static void log(Object logger, LogLevel level, Throwable throwable,
                           Supplier<String> msgSupplier, Object... args) {
        logExec(logger, level, throwable, msgSupplier, args);
    }

    /**
     * 打印日志
     * @param logger logger，不能为空，日志对象（Logger）
     * @param level level，为空时默认debug
     * @param throwable throwable，可以为空
     * @param msgSupplier 消息提供商，用于延迟初始化
     * @param args 参数
     */
    private static void logExec(Object logger, LogLevel level, Throwable throwable,
                                Supplier<String> msgSupplier, Object[] args) {
        level = level == null ? LogLevel.DEBUG : level;
        SERVICE.submit(
            new LogTask(logger, level, throwable, msgSupplier, args, checkLogger(logger, level)));
    }

    /**
     * 校验logger对象
     * @param logger logger对象
     * @param level 日志级别
     * @return 日志打印函数入参类型列表
     */
    private static Class[] checkLogger(Object logger, LogLevel level) {
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
                throw new LogException("不支持的日志：" + logger.getClass());
            }
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class LogTask implements Runnable {

        /**
         * 日志logger对象
         */
        private Object           logger;

        /**
         * 打印级别，必填
         */
        private LogLevel         level;

        /**
         * 异常，可空
         */
        private Throwable        throwable;

        /**
         * 日志消息供应商，起延迟加载的作用
         */
        private Supplier<String> msgSupplier;

        /**
         * 消息参数，可选
         */
        private Object[]         args;

        /**
         * 打印函数参数列表
         */
        private Class[]          paramterTypes;

        @Override
        public void run() {
            String msg = getMsgTemp();
            try {
                if (paramterTypes == JUL_PARAMETER_TYPES) {
                    // jul的日志打印
                    ReflectUtil.invoke(logger, "log", paramterTypes, convert(level), msg, args);
                } else {
                    ReflectUtil.invoke(logger, level.getName(), paramterTypes, msg, args);
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
        private Level convert(LogLevel level) {
            switch (level) {
                case DEBUG:
                    return Level.CONFIG;
                case INFO:
                    return Level.INFO;
                case WARN:
                    return Level.WARNING;
                case ERROR:
                    return Level.SEVERE;
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
    }
}
