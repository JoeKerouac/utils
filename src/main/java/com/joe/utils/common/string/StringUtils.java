package com.joe.utils.common.string;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.joe.utils.common.Assert;
import com.joe.utils.pattern.PatternUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 字符串的常用操作
 *
 * @author joe
 */
@Slf4j
public class StringUtils {

    private static final String                            charsets[]       = new String[] { "UTF-8",
                                                                                             "UTF-16",
                                                                                             "UTF-16LE",
                                                                                             "UTF-16BE",
                                                                                             "UTF-32",
                                                                                             "ISO-8859-1",
                                                                                             "US-ASCII",
                                                                                             "GBK",
                                                                                             "GB2312" };

    private static final StringGroupWraperFunction<String> DEFAULT_FUNCTION = String::toString;

    /**
     * 正则提取，从字符串中正则提取出指定表达式，默认提取第一个括号中的内容，没有则报错，并且将结果使用指定分隔符拼接
     * @param source 数据源
     * @param pattern 正则表达式，不能为空
     * @param separator 分隔符
     * @return 提取到的数组
     */
    public static String patternCollectAndReduce(String source, String pattern, String separator) {
        List<String> result = patternCollect(source, pattern, new int[] { 1 }, DEFAULT_FUNCTION);
        return result.stream().reduce((arg0, arg1) -> arg0 + separator + arg1).get();
    }

    /**
     * 正则提取，从字符串中正则提取出指定表达式，默认提取第一个括号中的内容，没有则报错
     * @param source 数据源
     * @param pattern 正则表达式，不能为空
     * @return 提取到的数组
     */
    public static List<String> patternCollect(String source, String pattern) {
        return patternCollect(source, pattern, new int[] { 1 }, DEFAULT_FUNCTION);
    }

    /**
     * 正则提取，从字符串中正则提取出指定表达式，默认提取第一个括号中的内容，没有则报错
     * @param source 数据源
     * @param pattern 正则表达式，不能为空
     * @param function 转换函数，不能为空，将提取到的数据转换为指定数据
     * @param <T> 转换后的数据类型
     * @return 提取到的数组
     */
    public static <T> List<T> patternCollect(String source, String pattern,
                                             StringGroupFunction<T> function) {
        return patternCollect(source, pattern, new int[] { 1 }, function);
    }

    /**
     * 正则提取，从字符串中正则提取出指定表达式
     * @param source 数据源
     * @param pattern 正则表达式，不能为空
     * @param groups 要提取的匹配到的group列表，不能为空
     * @return 提取到的数组
     */
    public static List<String> patternCollect(String source, String pattern, int[] groups) {
        return patternCollect(source, pattern, groups, DEFAULT_FUNCTION);
    }

    /**
     * 正则提取，从字符串中正则提取出指定表达式，例：
     * 
     * <li>source:"#123,12#,#456,45#,#789,78#"</li>
     * <li>pattern:".*?#([0-9]*),([0-9]*)#.*?"</li>
     * <li>groups:[1,2]</li>
     * 对于以上入参，提供给function的入参分别是：
     * <li>[123,12]</li>
     * <li>[456,45]</li>
     * <li>[789,78]</li>
     * 会循环调用function三次，每次入参数组长度为2
     * 
     * 
     * @param source 数据源
     * @param pattern 正则表达式，不能为空
     * @param groups 要提取的匹配到的group，不能为空。注意：groups必须不能超过正则表达式所能提取的最大值，例如正则中一个括号是一个group，正则本身也是一个group，详情参照测试用例
     * @param function 转换函数，不能为空，将提取到的数据转换为指定数据
     * @param <T> 转换后的数据类型
     * @return 提取到的数组
     */
    public static <T> List<T> patternCollect(String source, String pattern, int[] groups,
                                             StringGroupFunction<T> function) {
        Assert.notBlank(pattern, "pattern must not be blank");
        Assert.notNull(function, "function must not be null");
        Assert.isTrue(groups != null && groups.length > 0, "groups must not empty");

        if (isEmpty(source)) {
            return Collections.emptyList();
        }

        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(source);
        List<T> list = new ArrayList<>();
        while (matcher.find()) {
            List<String> patternGroup = new ArrayList<>(groups.length);

            for (int group : groups) {
                patternGroup.add(matcher.group(group));
            }

            list.add(function.apply(patternGroup));
        }
        return list;
    }

    /**
     * 比较两个字符串是否相等（调用equals方法）
     * @param arg0 第一个字符串
     * @param arg1 第二个字符串
     * @return
     */
    public static boolean equals(String arg0, String arg1) {
        return Objects.equals(arg0, arg1);
    }

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
        return MessageFormat.format(templet, args);
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

        HashMap<String, String> map = new HashMap<>((params.length / 3 + 1) * 4);
        for (String str : params) {
            String[] datas = str.split("=");
            if (datas.length > 1) {
                map.put(datas[0], datas[1]);
            } else {
                map.put(datas[0], null);
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
     * 判断字符串是否不为空（为null时也是空，字符串全是空白符时也是空）
     *
     * @param arg 指定字符串
     * @return 如果不为空则返回true
     */
    public static boolean isNotEmpty(String arg) {
        return !isEmpty(arg);
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
     * 判断字符串参数是否是数字（除了0.xx形式的数字，其他数字开头不能是0，例如00.1、01.1、03，上述这些都会返回false）
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
     * @return 两个字符串的最大公共子序列的长度，例：
     * <ul>
     *     <li>123456和456789的lcs为3</li>
     *     <li>123456和256789的lcs为3</li>
     *     <li>123456和556489的lcs为2</li>
     * </ul>
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
