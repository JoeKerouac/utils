package com.joe.utils.log.log4j2.plugin;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;

/**
 * @author joe
 * @version 2018.07.18 13:05
 */
public class Log4j2Test {
    /**
     * 检查logger的info级别设置是否成功
     * @param logger logger
     */
    public static void checkInfo(org.slf4j.Logger logger) {
        Assert.assertFalse(logger.isDebugEnabled());
        Assert.assertTrue(logger.isInfoEnabled());
    }

    /**
     * 检查logger的info级别设置是否成功
     * @param logger logger
     */
    public static void checkInfo(Logger logger) {
        Assert.assertFalse(logger.isDebugEnabled());
        Assert.assertTrue(logger.isInfoEnabled());
    }

    /**
     * 检查logger的debug级别设置是否成功
     * @param logger logger
     */
    public static void checkDebug(org.slf4j.Logger logger) {
        Assert.assertTrue(logger.isDebugEnabled());
        Assert.assertTrue(logger.isInfoEnabled());
    }

    /**
     * 检查logger的debug级别设置是否成功
     * @param logger logger
     */
    public static void checkDebug(Logger logger) {
        Assert.assertTrue(logger.isDebugEnabled());
        Assert.assertTrue(logger.isInfoEnabled());
    }
}
