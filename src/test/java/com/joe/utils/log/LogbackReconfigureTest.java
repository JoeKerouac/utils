package com.joe.utils.log;

import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

/**
 * @author joe
 * @version 2018.05.17 17:50
 */
@Slf4j
public class LogbackReconfigureTest {
    private InputStream config;
    private LogbackReconfigure logbackReconfigure;

    @Before
    public void init() {
        config = LogbackReconfigureTest.class.getClassLoader().getResourceAsStream("error.xml");
        logbackReconfigure = new LogbackReconfigure();
    }

    @Test
    public void doReconfigure() {
        String level = ((Logger) log).getEffectiveLevel().toString();
        Assert.assertEquals("ERROR", level);
        //更改logback配置
        logbackReconfigure.reconfigure(config);
        level = ((Logger) log).getEffectiveLevel().toString();
        Assert.assertEquals("INFO", level);
    }
}
