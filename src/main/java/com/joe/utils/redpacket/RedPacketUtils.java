package com.joe.utils.redpacket;

import com.joe.utils.common.Assert;

import java.security.SecureRandom;

/**
 * 红包工具
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年10月17日 上午11:00 JoeKerouac Exp $
 */
public class RedPacketUtils {

    /**
     * 分红包
     * @param amount 红包总额
     * @param count 红包个数
     * @param min 红包最小值
     * @param max 红包最大值
     * @return 红包数组
     */
    public static int[] split(int amount, int count, int min, int max) {
        Assert.isPositive(amount, "amount必须大于0");
        Assert.isPositive(count, "count必须大于0");
        Assert.isPositive(min, "min必须大于0");
        Assert.isPositive(max, "max必须大于0");

        if (amount < count * min) {
            throw new IllegalArgumentException("参数错误，min * count 不能大于amount");
        }

        if (amount > max * count) {
            throw new IllegalArgumentException("参数错误，max * count 不能小于amount");
        }

        if (min > max) {
            throw new IllegalArgumentException("参数错误，min不能大于max");
        }

        SecureRandom secureRandom = new SecureRandom();
        int nowAmount = amount;

        int[] array = new int[count];

        for (int i = 0; i < count; i++) {
            if (i == (count - 1)) {
                array[i] = nowAmount;
            } else {
                int random = Math.abs(secureRandom.nextInt());
                int nowMax = nowAmount - ((count - i - 1) * min);
                int nowMin = nowAmount - ((count - i - 1) * max);
                //当前允许最小值
                nowMax = Math.min(nowMax, max);
                //当前允许最大值
                nowMin = Math.max(nowMin, min);
                //区间
                int mod = nowMax - nowMin;
                //当前值
                int packet;
                if (mod == 0) {
                    // 此时说明后续的红包都是最小的了
                    packet = nowMin;
                } else {
                    packet = random % mod + nowMin;
                }

                array[i] = packet;
                nowAmount -= packet;
            }
        }
        return array;
    }
}
