package com.joe.utils.concurrent;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author joe
 * @version 2018.08.08 15:14
 */
public class LockServiceTest {
    private static final String KEY = "LockServiceTest";

    @Test
    public void test() throws Exception {
        LockService.lock(KEY);
        Thread thread = new Thread(() -> {
            try {
                Assert.assertFalse(LockService.tryLock(KEY, 10));
            } catch (Exception e) {
                Assert.assertNull(e);
            }

        });
        thread.start();
        thread.join();
        LockService.unlock(KEY);
    }
}
