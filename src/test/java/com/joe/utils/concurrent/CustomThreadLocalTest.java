package com.joe.utils.concurrent;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

import com.joe.utils.common.StringUtils;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年04月09日 20:54 JoeKerouac Exp $
 */
public class CustomThreadLocalTest {

    /**
     * 测试正常场景
     */
    @Test
    public void doTestGet() {
        // 测试一层
        ThreadLocal<String> threadLocal = new CustomThreadLocal<>();
        threadLocal.set("123");

        AtomicBoolean flag = new AtomicBoolean(false);
        //        Thread t1 = new CustomThread(() -> flag.set(StringUtils.isNotEmpty(threadLocal.get())));
        //        t1.start();
        //        ThreadUtil.join(t1);
        //        Assert.assertTrue(flag.get());

        flag.set(false);
        // 测试多层
        Thread t2 = new CustomThread(() -> {
            Thread t3 = new CustomThread(() -> flag.set(StringUtils.isNotEmpty(threadLocal.get())));
            t3.start();
            ThreadUtil.join(t3);
        });
        t2.start();

        ThreadUtil.join(t2);
        Assert.assertTrue(flag.get());
    }

    /**
     * 测试父线程结束的时候子线程无法获取父线程的ThreadLocal的场景
     * @throws InterruptedException InterruptedException
     */
    @Test
    public void doTestGetErr() throws InterruptedException {
        ThreadLocal<String> threadLocal = new CustomThreadLocal<>();
        AtomicBoolean flag = new AtomicBoolean(false);

        Thread t1 = new Thread(() -> flag.set(StringUtils.isEmpty(threadLocal.get())));

        Thread t2 = new Thread(() -> {
            threadLocal.set("123");
            t1.start();
        });

        t2.start();

        t2.join();
        t1.join();
        Assert.assertTrue(flag.get());
    }
}
