package com.joe.utils.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author joe
 * @version 2018.08.08 15:08
 */
public class ThreadUtilTest {

    @Test
    public void doCreatePool() throws Exception {
        ExecutorService service = ThreadUtil.createPool(ThreadUtil.PoolType.Calc);
        int count = 1000;
        CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            service.submit(() -> latch.countDown());
        }
        latch.await(5, TimeUnit.SECONDS);
        Assert.assertTrue(latch.getCount() == 0);
    }
}
