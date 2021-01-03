package com.joe.utils.common;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 农历的一些方法，目前只到2050年
 *
 * @author Winter Lau
 */
public class LunarCalendar {

    final private static long[] lunarInfo = new long[] {0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554,
        0x056a0, 0x09ad0, 0x055d2, 0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0,
        0x14977, 0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970, 0x06566,
        0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950, 0x0d4a0, 0x1d8a6, 0x0b550,
        0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, 0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0,
        0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0, 0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263,
        0x0d950, 0x05b57, 0x056a0, 0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0,
        0x195a6, 0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570, 0x04af5,
        0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0, 0x0c960, 0x0d954, 0x0d4a0,
        0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5, 0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9,
        0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, 0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0,
        0x0d260, 0x0ea65, 0x0d530, 0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520,
        0x0dd45, 0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0};

    // 天干
    private final static String[] Gan = new String[] {"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
    // 地支
    private final static String[] Zhi = new String[] {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};
    // 属相
    private final static String[] Animals = new String[] {"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};

    /**
     * 计算指定年份对应的生肖
     *
     * @param year
     *            指定年份
     * @return 指定年份对应的生肖
     */
    public static String animalsYear(int year) {
        return Animals[(year - 4) % 12];
    }

    /**
     * 计算指定年份的天干地支（例如1995年是乙亥年）
     *
     * @param year
     *            指定年份
     * @return 指定年份对应的天干地支
     */
    public static String cyclical(int year) {
        int num = year - 1900 + 36;
        return (Gan[num % 10] + Zhi[num % 12]);
    }

    /**
     * 计算指定年月日对应的农历年月日
     *
     * @param year
     *            指定年
     * @param month
     *            指定月
     * @param day
     *            指定日
     * @return 农历的年月日数组，数组第一个为年，第二个为月，第三个为日
     */
    public static int[] calElement(int year, int month, int day) {
        int[] nongDate = new int[3];
        int i, temp = 0, leap;
        // Date baseDate = new Date(0, 0, 31);
        Date baseDate = new GregorianCalendar(1900, 0, 31).getTime();
        // Date objDate = new Date(y - 1900, m - 1, d);
        Date objDate = new GregorianCalendar(year, month - 1, day).getTime();
        // 此处必须四舍五入
        int offset = Math.round((objDate.getTime() - baseDate.getTime()) / 86400000F);

        for (i = 1900; i < 2050 && offset > 0; i++) {
            temp = lYearDays(i);
            offset -= temp;
        }
        if (offset < 0) {
            offset += temp;
            i--;
        }
        nongDate[0] = i;
        leap = leapMonth(i); // 闰哪个月

        for (i = 1; i < 13 && offset > 0; i++) {
            // 闰月
            if (leap > 0 && i == (leap + 1)) {
                --i;
                temp = leapDays(nongDate[0]);
            } else {
                temp = monthDays(nongDate[0], i);
            }
            offset -= temp;
        }

        if (offset < 0) {
            offset += temp;
            --i;
        }
        nongDate[1] = i;
        nongDate[2] = offset + 1;
        return nongDate;
    }

    /**
     * 获取指定农历日的中国常用叫法（例如入1，返回初一）
     *
     * @param day
     *            农历的日
     * @return 农历的日的常用叫法
     */
    public static String getChinaDate(int day) {
        String a = "";
        if (day == 10)
            return "初十";
        if (day == 20)
            return "二十";
        if (day == 30)
            return "三十";
        int two = (day) / 10;
        if (two == 0)
            a = "初";
        if (two == 1)
            a = "十";
        if (two == 2)
            a = "廿";
        int one = day % 10;
        switch (one) {
            case 1:
                a += "一";
                break;
            case 2:
                a += "二";
                break;
            case 3:
                a += "三";
                break;
            case 4:
                a += "四";
                break;
            case 5:
                a += "五";
                break;
            case 6:
                a += "六";
                break;
            case 7:
                a += "七";
                break;
            case 8:
                a += "八";
                break;
            case 9:
                a += "九";
                break;
        }
        return a;
    }

    /**
     * 获取今天的农历日期
     *
     * @return 今天的农历日期数组，数组第一个元素是年，第二个元素是月，第三个元素是日
     */
    public static int[] today() {
        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH) + 1;
        int date = today.get(Calendar.DATE);
        return calElement(year, month, date);
    }

    /**
     * 计算指定年份农历有多少天
     *
     * @param year
     *            指定年份
     * @return 指定年份的弄农历有多少天
     */
    private static int lYearDays(int year) {
        int i, sum = 348;
        for (i = 0x8000; i > 0x8; i >>= 1) {
            if ((lunarInfo[year - 1900] & i) != 0)
                sum += 1;
        }
        return (sum + leapDays(year));
    }

    /**
     * 计算指定农历年闰月的天数
     *
     * @param year
     *            指定农历年
     * @return 指定农历年闰月的天数
     */
    private static int leapDays(int year) {
        if (leapMonth(year) != 0) {
            if ((lunarInfo[year - 1900] & 0x10000) != 0)
                return 30;
            else
                return 29;
        } else
            return 0;
    }

    /**
     * 计算指定农历年份农历闰哪个月（从1到12）
     *
     * @param year
     *            指定年份
     * @return 闰月
     */
    private static int leapMonth(int year) {
        return (int)(lunarInfo[year - 1900] & 0xf);
    }

    /**
     * 计算指定农历年月有多少天
     *
     * @param year
     *            指定农历年
     * @param month
     *            指定农历月
     * @return 指定年的月份的天数
     */
    private static int monthDays(int year, int month) {
        if ((lunarInfo[year - 1900] & (0x10000 >> month)) == 0)
            return 29;
        else
            return 30;
    }
}
