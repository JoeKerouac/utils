package com.joe.utils.log.log4j2.plugin.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import com.joe.utils.log.log4j2.plugin.Log4j2Test;

/**
 * 该单元测试直接运行通不过（因为添加的有logback的依赖），需要将logback的依赖删除才能测试，此处代码仅用于参考。
 *
 * 测试方法：新建一个maven项目，导入该依赖，同时提供log4j2的依赖（需要slf4j的话同时应该提供slf4j），然后可以将此处代码
 * 复制出去做单元测试，如果想直接运行该单元测试，那么需要将{@link com.joe.utils.log.LogbackReconfigure LogbackReconfigure}
 * 和{@link com.joe.utils.log.LogbackReconfigureTest LogbackReconfigureTest}的代码注释掉，然后将pom中logback的依赖注释
 * 掉，最后即可运行该单元测试。
 *
 * @author joe
 * @version 2018.07.18 13:05
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringPropUtilTest.class, properties = { "level=INFO" })
@ComponentScan("com.joe.utils.log.log4j2.plugin.spring")
public class SpringPropUtilTest {
    @Test
    public void doTest() {
        Logger logger = LoggerFactory.getLogger(SpringPropUtilTest.class);
        Log4j2Test.checkInfo(logger);
    }
}
