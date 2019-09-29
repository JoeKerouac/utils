package com.joe.utils.log;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

/**
 * 日志操作测试
 *
 * @author JoeKerouac
 * @version 2019年09月19日 20:03
 */
@Slf4j
public class LoggerOperateTest extends LogBaseTest {

    private static final String LOGGER_NAME = "com.joe";

    @Test
    public void testLoggerOperate() {
        Object logbackLogger = getLogbackLogger(LOGGER_NAME);
        Object log4jLogger = getLog4jLogger(LOGGER_NAME);

        Assert.assertTrue(logbackLogger.getClass().getName().contains("logback"));
        Assert.assertTrue(log4jLogger.getClass().getName().contains("apache"));

        invokeTest(logbackLogger);
        invokeTest(log4jLogger);
        LogOperate logOperate = LoggerOperateFactory.getConverter(log);
        logOperate.mdcPut("123", "123");
        Assert.assertTrue(!logOperate.getAllLogger().isEmpty());
    }

    private void invokeTest(Object logger) {
        LogOperate logOperate = LoggerOperateFactory.getConverter(logger);
        logOperate.setLevel(logger, LogLevel.ERROR);
        LogLevel level = logOperate.getLevel(logger);
        Assert.assertEquals(LogLevel.ERROR, level);
        Assert.assertEquals(LOGGER_NAME, logOperate.getName(logger));
        Assert.assertTrue(!logOperate.getAllLogger(logger.getClass().getClassLoader()).isEmpty());
        logOperate.mdcPut(logger.getClass().getClassLoader(), "123", "123");
    }
}
