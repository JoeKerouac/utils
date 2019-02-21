package com.joe.utils.log.log4j2.plugin.spring;

import org.apache.logging.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.joe.utils.log.log4j2.plugin.Log4j2Test;
import com.joe.utils.log.log4j2.plugin.impl.DefaultPropUtilTest;
import com.joe.utils.test.BaseTest;

/**
 * 集成spring的单元测试
 *
 * @author joe
 * @version 2018.07.18 13:05
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringPropUtilTest.class, properties = { "level=INFO" })
public class SpringPropUtilTest extends BaseTest {

    @Autowired
    private ApplicationContext context;

    /**
     * 测试是否可以发现
     */
    @Test
    public void doTestLog4j2() {
        runCase(() -> {
            SpringPropUtil.reconfigLog4j2(context);
            org.apache.logging.log4j.Logger logger = LogManager
                .getLogger(DefaultPropUtilTest.class);
            Log4j2Test.checkInfo(logger);
        });
    }

    @Before
    public void junitInit() {
        skipAll(true);
    }
}
