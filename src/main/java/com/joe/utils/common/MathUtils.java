package com.joe.utils.common;

/**
 * 数学工具类
 *
 * @author joe
 * @version 2018.04.09 22:38
 */
public class MathUtils {

    /**
     * 计算阶乘
     *
     * @param arg 要计算的参数，必须大于0
     * @return 阶乘计算结果（如果结果大于long可以表示的最大值将会出现不确定结果）
     */
    public static long factorial(int arg) {
        if (arg <= 0) {
            throw new IllegalArgumentException("参数必须大于0");
        }
        long result = 1;
        for (int i = arg; i > 0; i--) {
            result *= i;
        }

        return result;
    }
}
