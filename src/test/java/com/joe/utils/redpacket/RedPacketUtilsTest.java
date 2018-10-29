package com.joe.utils.redpacket;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年10月17日 上午11:08 JoeKerouac Exp $
 */
public class RedPacketUtilsTest {
    @Test
    public void doSplit() {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            int amount = Math.abs(random.nextInt()) % 100 + 200;
            int count = 20;
            int min = 5;
            int max = 50;
            check(amount, count, min, max);
        }
    }

    /**
     * 测试红包程序是否正确
     * @param amount 总金额
     * @param count 总数量
     * @param min 最小值
     * @param max 最大值
     */
    private void check(int amount, int count, int min, int max) {
        int[] packets = RedPacketUtils.split(amount, count, min, max);

        long error = Arrays.stream(packets).filter(n -> n > max).filter(n -> n < min).count();
        if (error > 0) {
            throw new RuntimeException("异常，有红包超过最大值或者小于最小值");
        }
        int sum = Arrays.stream(packets).sum();
        if (sum != amount) {
            throw new RuntimeException("异常，红包总金额不对");
        }
    }

}
