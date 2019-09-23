package com.joe.utils.log;

import java.util.logging.Logger;

import org.junit.Test;
import org.slf4j.MDC;

import com.joe.utils.concurrent.ThreadUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author JoeKerouac
 * @version 2019年09月17日 15:50
 */
@Slf4j
public class LogUtilTest {
    private static final Logger JUL_LOGGER = Logger.getLogger("JUL_LOGGER");

    @Test
    public void test() {
        // 顺便搞下MDC，不过这个只能肉眼看打印结果，不能直接断言
        MDC.put("USER", "-JoeKerouac-");
        LogUtil util = new LogUtil();
        util.log(JUL_LOGGER, LogLevel.INFO, "JUL_LOGGER test log");
        util.log(log, LogLevel.INFO, "SLF4J test log");
        ThreadUtil.sleep(1);
    }

}
