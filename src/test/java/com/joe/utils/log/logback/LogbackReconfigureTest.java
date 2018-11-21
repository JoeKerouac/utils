package com.joe.utils.log.logback;

import java.io.InputStream;
import java.util.concurrent.Semaphore;

import com.joe.utils.test.BaseTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

import com.joe.utils.log.logback.spring.SpringBootLogbackReconfigure;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author joe
 * @version 2018.05.17 17:50
 */
@Slf4j
public class LogbackReconfigureTest extends BaseTest {
    /**
     * 测试使用LogbackReconfigure重新配置logback
     */
    @Test
    public void doReconfigureByContext()  {
        runCase(() -> {
            assertLevel("INFO");
            //更改logback配置
            reconfig(false);
            assertLevel("ERROR");
        });
    }

    /**
     * 测试在spring-boot环境下LogbackReconfigure重新配置失效
     */
    @Test
    public void doLogbackInSpringBoot()  {
        runCase(() -> {
            assertLevel("INFO");
            //使用logback重配
            reconfig(false);
            assertLevel("ERROR");
            //启动spring上下文
            SpringApplication.run(LogbackReconfigureTest.class, new String[] {});
            //之前的修改失效
            assertLevel("INFO");
        });
    }

    /**
     * 测试SpringBootLogbackReconfigure
     */
    @Test
    public void doSpringBootLogbackReconfigure()  {
        runCase(() -> {
            assertLevel("INFO");
            //使用SpringBootLogbackReconfigure重配logback
            reconfig(true);
            //启动spring-boot上下文前logback仍然是原来的配置
            assertLevel("INFO");
            //启动spring上下文
            SpringApplication.run(LogbackReconfigureTest.class, new String[] {});
            //启动spring-boot上下文后logback配置更改
            assertLevel("ERROR");
            //spring-boot启动起来后仍然可以使用LogbackReconfigure修改logback配置
            LogbackReconfigure.reconfigure(
                LogbackReconfigureTest.class.getClassLoader().getResourceAsStream("logback.xml"));
            assertLevel("INFO");
        });
    }

    /**
     * 重新配置logback
     * @param spring 是否使用spring的方式
     */
    private void reconfig(boolean spring) {
        if (spring) {
            SpringBootLogbackReconfigure.reconfigure("error.xml");
        } else {
            LogbackReconfigure.reconfigure(
                LogbackReconfigureTest.class.getClassLoader().getResourceAsStream("error.xml"));
        }
    }

    @Before
    public void junitInit() {
        skipAll(true);
    }

    @Override
    protected void init() {
        reset();
    }

    /**
     * 重置
     */
    private void reset() {
        InputStream config = LogbackReconfigureTest.class.getClassLoader()
            .getResourceAsStream("logback.xml");
        LogbackReconfigure.reconfigure(config, (LoggerContext) LoggerFactory.getILoggerFactory());
        SpringBootLogbackReconfigure.reconfigure("logback.xml");
    }

    /**
     * 验证当前日志是否是指定级别
     * @param level 级别
     */
    public static void assertLevel(String level) {
        String now = ((Logger) log).getEffectiveLevel().toString();
        Assert.assertEquals(level, now);
    }
}
