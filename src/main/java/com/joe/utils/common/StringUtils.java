package com.joe.utils.common;

import com.joe.utils.pattern.PatternUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 字符串的常用操作
 *
 * @author joe
 */
@Slf4j
public class StringUtils {
    /**
     * 删除字符串的前/后缀
     *
     * @param str 字符串
     * @param arg 要删除的前/后缀
     * @return 删除后的结果，例如当str是001234000、arg是0时，结果为1234
     */
    public static String trim(String str, String arg) {
        if (str == null || arg == null) {
            throw new NullPointerException("str or arg must not be null");
        }

        while (str.startsWith(arg)) {
            str = str.substring(arg.length());
        }

        while (str.endsWith(arg)) {
            str = str.substring(0, str.length() - arg.length());
        }
        return str;
    }

    /**
     * 解析form格式的参数，例如abc=123&amp;sdjk=234n这种格式的数据
     *
     * @param data 要解析的数据
     * @return 解析出来的数据
     */
    public static Map<String, String> parseForm(String data) {
        log.info("解析URL参数:[{}]", data);
        if (isEmpty(data)) {
            return Collections.emptyMap();
        }
        String[] params = data.split("&");
        Map<String, String> map = new HashMap<>();
        for (String str : params) {
            String strs[] = str.split("=");
            if (strs.length > 1) {
                map.put(strs[0], strs[1]);
            } else {
                map.put(strs[0], null);
            }
        }
        log.info("URL参数[{}]解析为[{}]", data, map);
        return map;
    }

    /**
     * 判断字符串是否为空（为null时也是空，字符串全是空白符时也是空）
     *
     * @param arg 指定字符串
     * @return 如果为空则返回true
     */
    public static boolean isEmpty(String arg) {
        return arg == null || arg.trim().isEmpty();
    }

    /**
     * 判断参数列表是否有空值
     *
     * @param args 参数列表
     * @return 如果参数列表有任意一个值为空则返回true，否则返回false
     */
    public static boolean isEmptyAny(String... args) {
        for (String arg : args) {
            if (isEmpty(arg)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断参数列表是否全为空
     *
     * @param args 参数列表
     * @return 如果参数列表全为空则返回true，否则有任意一个值不为空就返回false
     */
    public static boolean isEmptyAll(String... args) {
        for (String arg : args) {
            if (!isEmpty(arg)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串参数是否是数字
     *
     * @param arg 数字参数
     * @return 如果参数是数字则返回<code>true</code>
     */
    public static boolean isNumber(String arg) {
        return PatternUtils.isNumber(arg);
    }

    /**
     * 将首字母大写
     *
     * @param arg 指定字符串
     * @return 首字母大写后的字符串
     */
    public static String toFirstUpperCase(String arg) {
        return arg.substring(0, 1).toUpperCase() + arg.substring(1);
    }

    /**
     * 求两个字符串的最大公共子序列的长度
     *
     * @param arg0 字符串1
     * @param arg1 字符串2
     * @return 两个字符串的最大公共子序列的长度
     */

    public static long lcs(String arg0, String arg1) {
        if (arg0 == null || arg1 == null) {
            return 0;
        }
        return lcs(arg0, arg1, 0, 0);
    }

    /**
     * 求两个字符串的最大公共子序列的长度
     *
     * @param arg0 字符串1
     * @param arg1 字符串2
     * @param i    字符串1的当前位置指针
     * @param j    字符串2的当前位置指针
     * @return 两个字符串的最大公共子序列的长度
     */
    private static long lcs(String arg0, String arg1, int i, int j) {
        if (arg0.length() == i || arg1.length() == j) {
            return 0;
        }

        if (arg0.charAt(i) == arg1.charAt(j)) {
            return 1 + lcs(arg0, arg1, ++i, ++j);
        } else {
            return Math.max(lcs(arg0, arg1, ++i, j), lcs(arg0, arg1, i, ++j));
        }
    }
}
