package com.joe.utils.log.log4j2.plugin.impl;

import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import com.joe.utils.log.log4j2.plugin.Log4j2Test;

/**
 * @author joe
 * @version 2018.07.18 11:50
 */
public class DefaultPropUtilTest {
    /**
     * 测试使用slf4j+log4j2的时候动态更改配置
     */
    @Test
    public void doTestSlf4j() {
        ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
        //设置级别为INFO
        org.slf4j.Logger logger = iLoggerFactory.getLogger(DefaultPropUtilTest.class.getName());
        DefaultPropUtil.reconfigLog4j2(Collections.singletonMap("level", "INFO"));
        Log4j2Test.checkInfo(logger);

        //设置级别为DEBUG
        DefaultPropUtil.reconfigLog4j2(Collections.singletonMap("level", "DEBUG"));
        Log4j2Test.checkDebug(logger);
    }

    /**
     * 测试单独使用log4j2的时候动态更改配置
     */
    @Test
    public void doTestLog4j2() {
        Logger logger = LogManager.getLogger(DefaultPropUtilTest.class);

        //设置级别为DEBUG
        DefaultPropUtil.reconfigLog4j2(LogManager.getContext(false),
            Collections.singletonMap("level", "DEBUG"));
        Log4j2Test.checkDebug(logger);

        //设置级别为INFO
        DefaultPropUtil.reconfigLog4j2(LogManager.getContext(false),
            Collections.singletonMap("level", "INFO"));
        Log4j2Test.checkInfo(logger);
    }
}
