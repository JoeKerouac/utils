package com.joe.utils.log.logback;

import java.io.InputStream;

import ch.qos.logback.classic.LoggerContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

/**
 * @author joe
 * @version 2018.05.17 17:50
 */
@Slf4j
public class LogbackReconfigureTest {
    private InputStream config;

    @Before
    public void init() {
        config = LogbackReconfigureTest.class.getClassLoader().getResourceAsStream("error.xml");
    }

    @Test
    public void doReconfigure() {
        String level = ((Logger) log).getEffectiveLevel().toString();
        Assert.assertEquals("ERROR", level);
        //更改logback配置
        LogbackReconfigure.reconfigure(config);
        level = ((Logger) log).getEffectiveLevel().toString();
        Assert.assertEquals("INFO", level);
    }

    @Test
    public void doReconfigureByContext() {
        String level = ((Logger) log).getEffectiveLevel().toString();
        Assert.assertEquals("ERROR", level);
        //更改logback配置
        LogbackReconfigure.reconfigure(config , (LoggerContext)LoggerFactory.getILoggerFactory());
        level = ((Logger) log).getEffectiveLevel().toString();
        Assert.assertEquals("INFO", level);
    }
}
