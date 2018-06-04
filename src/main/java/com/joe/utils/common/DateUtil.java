package com.joe.utils.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 日期工具类
 * 该版本升级使用JDK8的全新日期API，同时年的格式化必须为小写y，也就是可以是yyyy，不能为YYYY（大写Y）
 *
 * @author joe
 */
public class DateUtil {
    private final static Logger logger = LoggerFactory.getLogger(DateUtil.class);
    /**
     * formatter缓存
     */
    private final static Map<String, DateTimeFormatter> FORMATTER_CACHE = new HashMap<>();
    /**
     * 常用格式化yyyy-MM-dd HH:mm:ss
     */
    public final static String BASE = "yyyy-MM-dd HH:mm:ss";
    /**
     * 常用格式化yyyy-MM-dd
     */
    public final static String SHORT = "yyyy-MM-dd";

    private DateUtil() {
    }

    /**
     * 获取指定年份的天数
     *
     * @param date 指定年份
     * @return 指定年份对应的天数
     */
    public static int getYearDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取指定月份的天数
     *
     * @param date 指定月份
     * @return 该月份的天数
     */
    public static int getMonthDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * 将指定日期字符串按照指定格式转换为日期对象（如果传入的时间没有当前时分秒信息或者年月日信息则默认填充当前时间）
     *
     * @param date   格式化日期字符串
     * @param format 日期字符串的格式
     * @return 格式化日期字符串对应的日期对象
     * @throws DateUtilException 格式错误时返回该异常
     */
    public static Date parse(String date, String format) {
        DateTimeFormatter formatter;
        //优先从缓存取，取不到创建一个，不用加锁
        if ((formatter = FORMATTER_CACHE.get(format)) == null) {
            formatter = DateTimeFormatter.ofPattern(format);
            FORMATTER_CACHE.put(format, formatter);
        }

        TemporalAccessor accessor = formatter.parse(date);
        LocalDateTime time;

        if (accessor.isSupported(ChronoField.DAY_OF_YEAR) && accessor.isSupported(ChronoField.SECOND_OF_DAY)) {
            time = LocalDateTime.from(accessor);
        } else if (accessor.isSupported(ChronoField.SECOND_OF_DAY)) {
            LocalTime localTime = LocalTime.from(accessor);
            time = localTime.atDate(LocalDate.now());
        } else if (accessor.isSupported(ChronoField.DAY_OF_YEAR)) {
            LocalDate localDate = LocalDate.from(accessor);
            time = localDate.atTime(LocalTime.now());
        } else {
            throw new RuntimeException("日期类解析异常，时间为：" + date + "；格式为：" + format);
        }

        return Date.from(time.toInstant(ZoneOffset.ofTotalSeconds(60 * 60 * 8)));
//        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(format));
//        LocalDateTime dateTime = LocalDateTime.of(localDate, LocalTime.of(0,0));
//        return Date.from(dateTime.toInstant(ZoneOffset.ofTotalSeconds(60 * 60 * 8)));
    }

    /**
     * 计算arg0-arg1的时间差
     *
     * @param arg0     arg0
     * @param arg1     arg1
     * @param dateUnit 返回结果的单位
     * @return arg0-arg1的时间差，精确到指定的单位（field）
     */
    public static long calc(Date arg0, Date arg1, DateUnit dateUnit) {
        return calc(arg0.toInstant(), arg1.toInstant(), dateUnit);
    }

    /**
     * 计算arg0-arg1的时间差
     *
     * @param arg0     日期字符串
     * @param arg1     日期字符串
     * @param format   日期字符串的格式
     * @param dateUnit 返回结果的单位
     * @return arg0-arg1的时间差，精确到指定的单位（field），出错时返回-1
     */
    public static long calc(String arg0, String arg1, String format, DateUnit dateUnit) {
        try {
            return calc(LocalDateTime.parse(arg0, DateTimeFormatter.ofPattern(format)), LocalDateTime.parse(arg1,
                    DateTimeFormatter.ofPattern(format)), dateUnit);
        } catch (Exception e) {
            logger.error("日期计算出错", e);
            return -1;
        }
    }

    /**
     * 将指定日期增加指定时长
     *
     * @param dateUnit 单位
     * @param amount   时长
     * @param date     指定日期
     * @param format   指定日期字符串的格式
     * @return 增加后的日期
     */
    public static Date add(DateUnit dateUnit, int amount, String date, String format) {
        LocalDateTime localDateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(format));
        localDateTime = localDateTime.plus(amount, create(dateUnit));
        return Date.from(localDateTime.toInstant(ZoneOffset.ofTotalSeconds(60 * 60 * 8)));
    }

    /**
     * 将指定日期加上指定的时长
     *
     * @param dateUnit 单位
     * @param amount   时长
     * @param date     指定的日期
     * @return 增加指定时长后的日期
     */
    public static Date add(DateUnit dateUnit, int amount, Date date) {
        return Date.from(date.toInstant().plus(amount, create(dateUnit)));
    }

    /**
     * 将当前日期加上指定的时长
     *
     * @param dateUnit 单位
     * @param amount   时长
     * @return 增加过指定时长的时间
     */
    public static Date add(DateUnit dateUnit, int amount) {
        return add(dateUnit, amount, new Date());
    }

    /**
     * 获取指定格式的当前日期的字符串
     *
     * @param format 日期格式
     * @return 指定格式的当前日期的字符串
     */
    public static String getFormatDate(String format) {
        return getFormatDate(format, new Date());
    }

    /**
     * 获取指定格式的当前日期的字符串，指定时区
     *
     * @param format 日期格式
     * @param zoneId 时区ID，例如GMT
     * @return 指定格式的当前日期的字符串
     */
    public static String getFormatDate(String format, String zoneId) {
        return getFormatDate(format, new Date(), zoneId);
    }

    /**
     * 获取指定日期的指定格式的字符串
     *
     * @param format 日期格式
     * @param date   指定日期
     * @return 指定日期的指定格式的字符串
     */
    public static String getFormatDate(String format, Date date) {
        return getFormatDate(format, date, ZoneId.systemDefault().getId());
    }

    /**
     * 获取指定日期的指定格式的字符串，指定时区
     *
     * @param format 日期格式
     * @param date   指定日期
     * @param zoneId 时区ID，例如GMT
     * @return 指定日期的指定格式的字符串
     */
    public static String getFormatDate(String format, Date date, String zoneId) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
        return dateTimeFormatter.format(date.toInstant().atZone(ZoneId.of(zoneId)).toLocalDateTime());
    }

    /**
     * 判断指定日期是否在当前时间之前，精确到指定单位
     *
     * @param date     指定日期
     * @param format   指定日期的格式
     * @param dateUnit 精确单位（例如传入年就是精确到年）
     * @return 如果指定日期在当前时间之前返回<code>true</code>
     */
    public static boolean beforeNow(String date, String format, DateUnit dateUnit) {
        logger.debug("指定日期为：{}", date);
        return calc(new Date(), parse(date, format), dateUnit) > 0;
    }

    /**
     * 查询时间是否在今日
     *
     * @param time 时间戳
     * @return 如果时间戳是今天的则返回<code>true</code>
     */
    public static boolean isToday(long time) {
        return isToday(new Date(time));
    }

    /**
     * 查询时间是否在今日
     *
     * @param date   时间字符串
     * @param format 时间字符串的格式
     * @return 如果指定日期对象在今天则返回<code>true</code>
     */
    public static boolean isToday(String date, String format) {
        String now = getFormatDate(SHORT);
        String target = getFormatDate(SHORT, parse(date, format));
        return now.equals(target);
    }

    /**
     * 查询时间是否在今日
     *
     * @param time 日期对象
     * @return 如果指定日期对象在今天则返回<code>true</code>
     */
    public static boolean isToday(Date time) {
        String now = getFormatDate(SHORT);
        String target = getFormatDate(SHORT, time);
        return now.equals(target);
    }

    public enum DateUnit {
        YEAR, MONTH, DAY, HOUR, MINUTE, SECOND
    }

    static class DateUtilException extends RuntimeException {
        private static final long serialVersionUID = 474205378026735176L;

        public DateUtilException(String message) {
            super(message);
        }
    }

    /**
     * 计算arg0-arg1的时间差
     *
     * @param arg0     arg0
     * @param arg1     arg1
     * @param dateUnit 返回结果的单位
     * @return arg0-arg1的时间差，精确到指定的单位（field），出错时返回-1
     */
    private static long calc(Temporal arg0, Temporal arg1, DateUnit dateUnit) {
        return create(dateUnit).between(arg1, arg0);
    }

    /**
     * 创建相应的ChronoUnit
     *
     * @param dateUnit 单位
     * @return ChronoUnit
     */
    private static ChronoUnit create(DateUnit dateUnit) {
        switch (dateUnit) {
            case YEAR:
                return ChronoUnit.YEARS;
            case MONTH:
                return ChronoUnit.MONTHS;
            case DAY:
                return ChronoUnit.DAYS;
            case HOUR:
                return ChronoUnit.HOURS;
            case MINUTE:
                return ChronoUnit.MINUTES;
            case SECOND:
                return ChronoUnit.SECONDS;
            default:
                throw new DateUtilException("没有单位：" + dateUnit);
        }
    }
}
