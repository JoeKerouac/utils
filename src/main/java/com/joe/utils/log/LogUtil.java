package com.joe.utils.log;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.joe.threadx.InterceptableThreadPoolExecutor;
import com.joe.threadx.InterceptableThreadPoolExecutorFactory;
import com.joe.threadx.ThreadPoolConfig;
import com.joe.threadx.ThreadxConst;
import com.joe.threadx.interceptor.mdc.MDCTaskInterceptor;
import com.joe.utils.common.Assert;

/**
 * 日志工具，提供日志模板延迟加载和日志异步打印的功能，同时支持slf4j
 *
 * @author JoeKerouac
 * @version 2019年09月17日 14:17
 */
public class LogUtil {

    /**
     * 日志队列堆积默认最大长度
     */
    private static final int                DEFAULT_QUEUE_SIZE = 10000 * 10;

    /**
     * 打印日志的线程池
     */
    private ExecutorService                 service;

    /**
     * 日志拦截器，对于返回false或者null的不会打印
     */
    private CopyOnWriteArrayList<LogFilter> filters;

    public LogUtil() {
        this(DEFAULT_QUEUE_SIZE);
    }

    public LogUtil(int queueSize) {
        this(queueSize, (LogFilter) null);
    }

    public LogUtil(int queueSize, LogFilter filter) {
        this(queueSize, Collections.singletonList(filter));
    }

    public LogUtil(int queueSize, List<LogFilter> filters) {
        Assert.notNull(filters, "filters不能为null");
        // 过滤空元素
        this.filters = filters.stream().filter(Objects::nonNull)
            .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        // 使用IO线程模型
        ThreadPoolConfig config = ThreadxConst.IO_THREAD_POO_CONFIG_SUPPLIER.get();
        // 指定队列长度
        config.setWorkQueue(new ArrayBlockingQueue<>(queueSize));
        InterceptableThreadPoolExecutor executor = InterceptableThreadPoolExecutorFactory
            .build(config, true);
        // 对线程池支持MDC功能
        executor.addLastTaskInterceptor(new MDCTaskInterceptor());
        this.service = executor;
    }

    /**
     * 打印日志
     * @param logger logger，不能为空，日志对象（Logger）
     * @param level level，为空时默认debug
     * @param msg 消息
     */
    public void log(Object logger, LogLevel level, String msg) {
        logSubmit(logger, level, null, () -> msg, null);
    }

    /**
     * 打印日志
     * @param logger logger，不能为空，日志对象（Logger）
     * @param level level，为空时默认debug
     * @param msg 消息
     * @param args 参数
     */
    public void log(Object logger, LogLevel level, String msg, Object... args) {
        logSubmit(logger, level, null, () -> msg, args);
    }

    /**
     * 打印日志
     * @param logger logger，不能为空，日志对象（Logger）
     * @param level level，为空时默认debug
     * @param msgSupplier 消息提供商，用于延迟初始化
     */
    public void log(Object logger, LogLevel level, Supplier<String> msgSupplier) {
        logSubmit(logger, level, null, msgSupplier, null);
    }

    /**
     * 打印日志
     * @param logger logger，不能为空，日志对象（Logger）
     * @param level level，为空时默认debug
     * @param msgSupplier 消息提供商，用于延迟初始化
     * @param args 参数
     */
    public void log(Object logger, LogLevel level, Supplier<String> msgSupplier, Object... args) {
        logSubmit(logger, level, null, msgSupplier, args);
    }

    /**
     * 打印日志
     * @param logger logger，不能为空，日志对象（Logger）
     * @param level level，为空时默认debug
     * @param throwable throwable，可以为空
     * @param msg 消息
     */
    public void log(Object logger, LogLevel level, Throwable throwable, String msg) {
        logSubmit(logger, level, throwable, () -> msg, null);
    }

    /**
     * 打印日志
     * @param logger logger，不能为空，日志对象（Logger）
     * @param level level，为空时默认debug
     * @param throwable throwable，可以为空
     * @param msg 消息
     * @param args 参数
     */
    public void log(Object logger, LogLevel level, Throwable throwable, String msg,
                    Object... args) {
        logSubmit(logger, level, throwable, () -> msg, args);
    }

    /**
     * 打印日志
     * @param logger logger，不能为空，日志对象（Logger）
     * @param level level，为空时默认debug
     * @param throwable throwable，可以为空
     * @param msgSupplier 消息提供商，用于延迟初始化
     */
    public void log(Object logger, LogLevel level, Throwable throwable,
                    Supplier<String> msgSupplier) {
        logSubmit(logger, level, throwable, msgSupplier, null);
    }

    /**
     * 打印日志
     * @param logger logger，不能为空，日志对象（Logger）
     * @param level level，为空时默认debug
     * @param throwable throwable，可以为空
     * @param msgSupplier 消息提供商，用于延迟初始化
     * @param args 参数
     */
    public void log(Object logger, LogLevel level, Throwable throwable,
                    Supplier<String> msgSupplier, Object... args) {
        logSubmit(logger, level, throwable, msgSupplier, args);
    }

    /**
     * 日志打印提交，后续将异步打印
     * @param logger logger，不能为空，日志对象（Logger）
     * @param level level，为空时默认debug
     * @param throwable throwable，可以为空
     * @param msgSupplier 消息提供商，用于延迟初始化
     * @param args 参数
     */
    private void logSubmit(Object logger, LogLevel level, Throwable throwable,
                           Supplier<String> msgSupplier, Object[] args) {
        level = level == null ? LogLevel.DEBUG : level;
        LogTask task = new LogTask(logger, level, throwable, msgSupplier, args);
        for (LogFilter filter : filters) {
            // 如果返回false或者null则拦截
            if (Boolean.TRUE != filter.apply(task)) {
                return;
            }
        }
        service.submit(task);
    }
}
