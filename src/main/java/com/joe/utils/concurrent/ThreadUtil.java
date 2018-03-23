package com.joe.utils.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程操作类
 *
 * @author joe
 */
public class ThreadUtil {
    private static final Logger logger = LoggerFactory.getLogger(ThreadUtil.class);
    private static final Map<PoolType, ExecutorService> cache = new HashMap<>();


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
            logger.warn("时间参数不正确或者线程被中断，线程将不会睡眠");
            throw new RuntimeException(e);
        }
    }

    /**
     * 当前线程睡眠一段时间（单位为秒），当线程被中断时不会抛出异常，如果
     * 你的实现依赖于该异常请勿调用该方法休眠
     *
     * @param time 时长
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
            String.format(format, 0);//检查是否符合格式
            ThreadFactory factory = new ThreadFactory() {
                AtomicInteger counter = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format(format, counter.getAndAdd(1)));
                }
            };
            synchronized (cache) {
                if (service == null || service.isTerminated() || service.isShutdown()) {
                    switch (type) {
                        case Singleton:
                            service = Executors.newSingleThreadExecutor(factory);
                            break;
                        case IO:
                            service = new ThreadPoolExecutor(30, 100, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>
                                    (), factory);
                            break;
                        case Calc:
                            service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), factory);
                            break;
                        default:
                            throw new IllegalArgumentException(String.format("当前参数为：%s；请使用正确的参数", type.toString()));
                    }
                }
                cache.put(type, service);
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
        String.format(format, 0);//检查是否符合格式
        //线程工厂
        ThreadFactory factory = new ThreadFactory() {
            AtomicInteger counter = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format(format, counter.getAndAdd(1)));
            }
        };
        ExecutorService service;
        switch (type) {
            case Singleton:
                service = Executors.newSingleThreadExecutor(factory);
                break;
            case IO:
                service = new ThreadPoolExecutor(Math.max(Runtime.getRuntime().availableProcessors() * 50, 80),
                        Math.max(Runtime.getRuntime().availableProcessors() * 150, 260), 30, TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(), factory);
                break;
            case Calc:
                service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), factory);
                break;
            default:
                throw new IllegalArgumentException(String.format("当前参数为：%s；请使用正确的参数", type.toString()));
        }
        return service;
    }

    public enum PoolType {
        Singleton, IO, Calc
    }
}
