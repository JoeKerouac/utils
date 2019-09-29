package com.joe.utils.log;

import java.util.logging.Logger;

import org.junit.Test;

import com.joe.utils.concurrent.ThreadUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author JoeKerouac
 * @version 2019年09月17日 15:50
 */
@Slf4j
public class LogUtilTest extends LogBaseTest {

    private static final String LOGGER_NAME = "com.joe.logger";

    private static final Logger JUL_LOGGER  = Logger.getLogger("JUL_LOGGER");

    private static LogUtil      LOG_UTIL    = new LogUtil();;

    @Test
    public void test() {
        // 顺便搞下MDC，不过这个只能肉眼看打印结果，不能直接断言
        LOG_UTIL.log(JUL_LOGGER, LogLevel.INFO, "JUL_LOGGER test log");

        invokeTest(getLogbackLogger(LOGGER_NAME));
        invokeTest(getLog4jLogger(LOGGER_NAME));

        ThreadUtil.sleep(1);
    }

    private void invokeTest(Object logger) {
        LoggerOperateFactory.getConverter(logger).mdcPut(logger.getClass().getClassLoader(), "USER",
            "-JoeKerouac-");
        LOG_UTIL.log(logger, LogLevel.INFO, logger + " test log");
    }

}
