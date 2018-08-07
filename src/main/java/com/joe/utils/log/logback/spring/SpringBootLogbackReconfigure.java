package com.joe.utils.log.logback.spring;

import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;

/**
 * 用来在spring-boot中指定logback的配置文件
 *
 * @author joe
 * @version 2018.08.07 13:53
 */
public class SpringBootLogbackReconfigure extends LogbackLoggingSystem {
    private static String xmlLocation = "logback.xml";

    public SpringBootLogbackReconfigure(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    protected String[] getStandardConfigLocations() {
        return new String[] { xmlLocation };
    }

    /**
     * 指定logback的配置文件，必须在ApplicationContext启动前使用，并且会在spring-boot上下文启动后生效
     * @param location logback配置文件
     */
    public static void reconfigure(String location) {
        System.setProperty(LoggingSystem.SYSTEM_PROPERTY,
            SpringBootLogbackReconfigure.class.getName());
        SpringBootLogbackReconfigure.xmlLocation = location;
    }
}
