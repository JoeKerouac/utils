package com.joe.utils.log.log4j2.plugin.impl;

import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.joe.utils.log.log4j2.plugin.Log4j2Test;
import com.joe.utils.test.BaseTest;

/**
 * @author joe
 * @version 2018.07.18 11:50
 */
public class DefaultPropUtilTest extends BaseTest {
    /**
     * 测试动态更改配置
     */
    @Test
    public void doTestLog4j2() {
        runCase(() -> {
            Logger logger = LogManager.getLogger(DefaultPropUtilTest.class);

            //设置级别为DEBUG
            DefaultPropUtil.reconfigLog4j2(LogManager.getContext(false),
                Collections.singletonMap("level", "DEBUG"));
            Log4j2Test.checkDebug(logger);

            //设置级别为INFO
            DefaultPropUtil.reconfigLog4j2(LogManager.getContext(false),
                Collections.singletonMap("level", "INFO"));
            Log4j2Test.checkInfo(logger);
        });
    }

    @Before
    public void junitInit() {
        skipAll(true);
    }
}
