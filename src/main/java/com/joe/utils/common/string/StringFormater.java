package com.joe.utils.common.string;

import java.text.MessageFormat;

/**
 * @author JoeKerouac
 * @version 2019年08月15日 19:43
 */
public class StringFormater {

    /**
     * 将字符串中的{}依次替换为指定数据，例如"ab{0}{1}{0}"
     * 
     * @param templet
     *            字符串模板
     * @param args
     *            变量
     * @return 字符串
     */
    public static String simpleFormat(String templet, Object... args) {
        return MessageFormat.format(templet, args);
    }

    /**
     * json格式化，注意：会遍历字符串，性能不会太好
     * 
     * @param jsonStr
     *            要格式化的json串（不会校验json格式）
     * @return 格式化后的字符串
     */
    public static String jsonFormat(String jsonStr) {
        StringBuilder sb = new StringBuilder();
        int tabCount = 0;
        for (int i = 0; i < jsonStr.length(); i++) {
            char nowChar = jsonStr.charAt(i);
            switch (nowChar) {
                case StringConst.LEFT_BRACES:
                case StringConst.LEFT_BRACKET:
                    sb.append(nowChar);
                    sb.append(StringConst.LINE_BREAK);
                    sb.append(StringUtils.copy(StringConst.TAB, ++tabCount));
                    break;
                case StringConst.RIGHT_BRACES:
                case StringConst.RIGHT_BRACKET:
                    sb.append(StringConst.LINE_BREAK);
                    tabCount = tabCount - 1;
                    if (tabCount > 0) {
                        sb.append(StringUtils.copy(StringConst.TAB, tabCount));
                    }
                    sb.append(nowChar);
                    break;
                case StringConst.COMMA:
                    sb.append(nowChar);
                    sb.append(StringConst.LINE_BREAK);
                    sb.append(StringUtils.copy(StringConst.TAB, tabCount));
                    break;
                default:
                    sb.append(nowChar);
                    break;
            }
        }
        return sb.toString();
    }
}
