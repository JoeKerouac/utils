package com.joe.utils.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.joe.utils.annotation.Nullable;
import com.joe.utils.pattern.PatternUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 字符串的常用操作
 *
 * @author joe
 */
@Slf4j
public class StringUtils {
    private static final String charsets[] = new String[] { "UTF-8", "UTF-16", "UTF-16LE",
                                                            "UTF-16BE", "UTF-32", "ISO-8859-1",
                                                            "US-ASCII", "GBK", "GB2312" };

    /**
     * 将目标字符串重复count次返回
     * @param str 目标字符串
     * @param count 次数
     * @return 目标字符串重复count次结果，例如目标字符串是test，count是2，则返回testtest，如果count是3则返回testtesttest
     */
    public static String copy(String str, int count) {
        if (str == null) {
            throw new NullPointerException("原始字符串不能为null");
        }

        if (count <= 0) {
            throw new IllegalArgumentException("次数必须大于0");
        }

        if (count == 1) {
            return str;
        }

        if (count == 2) {
            return str + str;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * 将字符串中的{}依次替换为指定数据
     * @param templet 字符串模板
     * @param args 变量
     * @return 字符串
     */
    public static String format(String templet, Object... args) {
        if (args == null || args.length == 0) {
            return templet;
        }
        for (Object arg : args) {
            templet = templet.replaceFirst("\\{}", String.valueOf(arg));
        }
        return templet;
    }

    /**
     * 替换指定结束位置之前的所有字符
     *
     * @param str 字符串
     * @param end 要替换的结束位置（包含该位置）
     * @param rp  替换字符串
     * @return 替换后的字符串，例如对123456替换3,*，结果为*56
     */
    public static String replaceBefor(String str, int end, String rp) {
        return replace(str, 0, end, rp);
    }

    /**
     * 替换指定起始位置之后的所有字符
     *
     * @param str   字符串
     * @param start 要替换的起始位置（包含该位置）
     * @param rp    替换字符串
     * @return 替换后的字符串，例如对123456替换3,*，结果为123*
     */
    public static String replaceAfter(String str, int start, String rp) {
        return replace(str, start, str.length() - 1, rp);
    }

    /**
     * 替换指定区间位置的所有字符
     *
     * @param str   字符串
     * @param start 要替换的起始位置（包含该位置）
     * @param end   要替换的结束位置（包含该位置）
     * @param rp    替换字符串
     * @return 替换后的字符串，例如对123456替换1,3,*，结果为1*56
     */
    public static String replace(String str, int start, int end, String rp) {
        if (str == null || start < 0 || start > end || end >= str.length()) {
            throw new IllegalArgumentException("参数非法");
        }

        return str.substring(0, start) + rp + str.substring(end + 1);
    }

    /**
     * 判断字符串长度是否大于0
     *
     * @param str 字符串
     * @return 长度大于0时返回true，字符串为null或者字符串长度等于0时返回false
     */
    public static boolean hasLength(String str) {
        return str != null && !str.isEmpty();
    }

    /**
     * 替换字符串中的字符
     *
     * @param inString   字符串
     * @param oldPattern 旧字符
     * @param newPattern 新字符
     * @return 替换后的字符串
     */
    public static String replace(String inString, String oldPattern, @Nullable String newPattern) {
        if (!hasLength(inString) || !hasLength(oldPattern) || newPattern == null) {
            return inString;
        }
        int index = inString.indexOf(oldPattern);
        if (index == -1) {
            // no occurrence -> can return input as-is
            return inString;
        }

        int capacity = inString.length();
        if (newPattern.length() > oldPattern.length()) {
            capacity += 16;
        }
        StringBuilder sb = new StringBuilder(capacity);

        int pos = 0; // our position in the old string
        int patLen = oldPattern.length();
        while (index >= 0) {
            sb.append(inString.substring(pos, index));
            sb.append(newPattern);
            pos = index + patLen;
            index = inString.indexOf(oldPattern, pos);
        }

        // append any characters to the right of a match
        sb.append(inString.substring(pos));
        return sb.toString();
    }

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
