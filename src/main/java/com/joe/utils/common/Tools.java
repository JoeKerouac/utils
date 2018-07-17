package com.joe.utils.common;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.UUID;

public class Tools {
    protected static final DecimalFormat NUMFORMAT = new DecimalFormat("#.00");

    /**
     * 判断指定数组dests中是否包含指定数据src
     * @param src 数据
     * @param dests 数组，不能为空
     * @return 返回true表示dests中包含src
     */
    public static boolean contains(Object src, Object... dests) {
        if (dests == null) {
            throw new NullPointerException("dest must not be null");
        }

        if (src == null) {
            return false;
        }

        for (Object dest : dests) {
            if (src == dest || src.equals(dest)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成一个32位的UUID
     *
     * @return 32位的UUID
     */
    public static String createUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 生成随机字符串（只包含数字和字母）
     *
     * @param length 字符串的长度
     * @return 返回指定长度的随机字符串，当长度小于等于0时返回空的字符串
     */
    public static String createRandomStr(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("长度不能小于0");
        }
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            int num = (int) (Math.random() * 75) + 48;
            while ((num > 57 && num < 65) || (num > 90 && num < 97)) {
                num = (int) (Math.random() * 75) + 48;
            }
            chars[i] = (char) num;
        }
        return new String(chars);
    }

    /**
     * 生成随机字符串（只包含数字）
     *
     * @param length 长度
     * @return 随机字符串
     */
    public static String createRandomNum(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("长度不能小于0");
        }
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = (char) ((Math.random() * 10) + 48);
        }
        return new String(chars);
    }

    /**
     * 处理浮点数，保留小数点后两位，不足的补0
     *
     * @param bigDecimal 浮点数
     * @return 处理结果，例如：1.00、1.10
     */
    public static String dealDouble(BigDecimal bigDecimal) {
        double count = bigDecimal.doubleValue();
        return dealDouble(count);
    }

    /**
     * 处理浮点数，保留小数点后两位，不足的补0
     *
     * @param count 浮点数
     * @return 处理结果，例如：1.00、1.10
     */
    public static String dealDouble(double count) {
        if (count < 0.005 && count > -0.005) {
            return "0.00";
        }
        String result = NUMFORMAT.format(count);
        return result.startsWith(".") ? "0" + result : result;
    }
}
