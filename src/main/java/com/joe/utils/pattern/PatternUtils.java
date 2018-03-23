package com.joe.utils.pattern;

import com.joe.utils.common.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则工具类
 *
 * @author joe
 * @version 2018.03.08 17:43
 */
public class PatternUtils {
    /**
     * IP正则
     */
    private static final Pattern IP_PATTERN = Pattern.compile("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}");
    /**
     * 手机号正则
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3|4|5|7|8]\\d{9}$");

    /**
     * 判断字符串是否是IP格式的字符串
     *
     * @param data 字符串
     * @return 返回true时表示是IP格式的字符串
     */
    public static boolean isIp(String data) {
        return check(data, IP_PATTERN);
    }

    /**
     * 校验字符串是否是手机号（不准确，仅仅判断是否是13、14、15、17、18开头）
     *
     * @param data 字符串
     * @return 返回true表示是手机号
     */
    public static boolean isPhone(String data) {
        return check(data, PHONE_PATTERN);
    }

    /**
     * 检查数据是否符合正则
     *
     * @param data    数据
     * @param pattern 正则
     * @return 返回true表示数据符合正则表达式（默认数据为空时返回false）
     */
    private static boolean check(String data, Pattern pattern) {
        if (StringUtils.isEmpty(data)) {
            return false;
        }
        Matcher matcher = pattern.matcher(data);
        return matcher.matches();
    }
}
