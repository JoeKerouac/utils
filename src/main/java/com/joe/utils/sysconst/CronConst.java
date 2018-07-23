package com.joe.utils.sysconst;

/**
 * cron表达式
 *
 * @author joe
 * @version 2018.07.23 16:27
 */
public final class CronConst {
    /**
     * 每秒执行一次
     */
    public static final String EVE_SECOND = "* * * * * ? *";
    /**
     * 每分钟执行一次
     */
    public static final String EVE_MINUTE = "0 * * * * ? *";
    /**
     * 每小时执行一次
     */
    public static final String EVE_HOURE = "0 0 * * * ? *";
    /**
     * 每天0点执行一次
     */
    public static final String EVE_ZORE_HOURE = "0 0 0 * * ? *";
    /**
     * 每月一号执行一次
     */
    public static final String EVE_FIRST_DAY = "0 0 0 1 * ? *";

}
