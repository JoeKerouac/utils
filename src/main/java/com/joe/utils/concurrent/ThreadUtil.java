package com.joe.utils.concurrent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.joe.utils.exception.ExceptionWraper;

import lombok.extern.slf4j.Slf4j;

/**
 * 线程操作类
 *
 * @author joe
 */
@Slf4j
public class ThreadUtil {
    private static final Map<PoolType, ExecutorService> cache           = new HashMap<>();
    private static final RejectedExecutionHandler       DEFAULT_HANDLER = new ThreadPoolExecutor.AbortPolicy();

    /**
     * {@link Thread#join()}操作，屏蔽InterruptedException异常，会将InterruptedException异常转换为WrapedInterruptedException异常
     * @param thread thread
     */
    public static void join(Thread thread) {
        ExceptionWraper.run(thread::join,
            e -> new WrapedInterruptedException((InterruptedException) e));
    }

    /**
     * 当前线程睡眠一段时间，当线程被中断时会抛出RuntimeException而不是InterruptedException，如果
     * 你的实现依赖于该异常请勿调用该方法休眠
     *
     * @param time 时长
     * @param unit 单位
     */
    public static void sleep(long time, TimeUnit unit) {
        try {
            if (time <= 0 || unit == null) {
                return;
            }

            Thread.sleep(unit.toMillis(time));
        } catch (InterruptedException e) {
            log.warn("时间参数不正确或者线程被中断，线程将不会睡眠");
            throw new RuntimeException(e);
        }
    }

    /**
     * 当前线程睡眠一段时间（单位为秒），当线程被中断时不会抛出异常，如果
     * 你的实现依赖于该异常请勿调用该方法休眠
     *
     * @param time 时长，单位为秒
     */
    public static void sleep(long time) {
        sleep(time, TimeUnit.SECONDS);
    }

    /**
     * 从缓存中查找指定类型的线程池，如果存在那么直接返回，如果不存在那么创建返回
     *
     * @param type 线程池类型
     * @return 指定类型的线程池
     */
    public static ExecutorService getOrCreatePool(PoolType type) {
        return getOrCreatePool(type, "custom-thread-%d");
    }

    /**
     * 从缓存中查找指定类型的线程池，如果存在那么直接返回，如果不存在那么创建返回
     *
     * @param type   线程池类型
     * @param format 线程名格式，格式为：format-%d，其中%d将被替换为从0开始的数字序列，不能为null
     * @return 指定类型的线程池
     */
    public static ExecutorService getOrCreatePool(PoolType type, String format) {
        ExecutorService service = cache.get(type);
        if (service == null || service.isTerminated() || service.isShutdown()) {
            //检查是否符合格式
            String.format(format, 0);

            synchronized (cache) {
                if (service == null || service.isTerminated() || service.isShutdown()) {
                    ThreadFactory factory = build(format);

                    service = build(type, factory);
                    cache.put(type, service);
                }
            }
        }
        return service;
    }

    /**
     * 创建指定类型的线程池
     *
     * @param type 线程池类型
     * @return 返回指定类型的线程池
     */
    public static ExecutorService createPool(PoolType type) {
        return createPool(type, "custom-thread-%d");
    }

    /**
     * 创建指定类型的线程池
     *
     * @param type   线程池类型
     * @param format 线程名格式，格式为：format-%d，其中%d将被替换为从0开始的数字序列，不能为null
     * @return 返回指定类型的线程池
     */
    public static ExecutorService createPool(PoolType type, String format) {
        //检查是否符合格式
        String.format(format, 0);
        //线程工厂
        ThreadFactory factory = new ThreadFactory() {
            AtomicInteger counter = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format(format, counter.getAndAdd(1)));
            }
        };

        return build(type, factory);
    }

    /**
     * 构建指定类型的线程池
     * @param type 线程池类型
     * @param factory 线程工厂，用来构建线程
     * @return 线程池
     */
    public static ExecutorService build(PoolType type, ThreadFactory factory) {
        int corePoolSize, maximumPoolSize;
        int keepAliveTime = 10;
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
        switch (type) {
            case Singleton:
                corePoolSize = maximumPoolSize = 1;
                break;
            case IO:
                corePoolSize = Runtime.getRuntime().availableProcessors() * 10;
                maximumPoolSize = Runtime.getRuntime().availableProcessors() * 20;
                break;
            case Calc:
                corePoolSize = maximumPoolSize = Runtime.getRuntime().availableProcessors() + 1;
                break;
            default:
                throw new IllegalArgumentException(
                    String.format("内部异常，未知线程池类型[%s]", type.toString()));
        }
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime,
            TimeUnit.SECONDS, workQueue, factory, DEFAULT_HANDLER);
    }

    /**
     * 构建ThreadFactory
     * @param format 线程名字模板，格式为：format-%d，其中%d将被替换为从0开始的数字序列，不能为null
     * @return ThreadFactory
     */
    private static ThreadFactory build(String format) {
        return new ThreadFactory() {
            AtomicInteger counter = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, String.format(format, counter.getAndAdd(1)));
                thread.setDaemon(true);
                return thread;
            }
        };
    }

    public enum PoolType {
                          Singleton, IO, Calc
    }
}
