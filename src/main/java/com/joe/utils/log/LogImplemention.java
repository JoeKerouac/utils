package com.joe.utils.log;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 日志实现类型说明
 *
 * @author JoeKerouac
 * @version 2019年09月18日 14:56
 */
@Data
@AllArgsConstructor
public class LogImplemention {

    /**
     * log4j
     */
    public static final LogImplemention LOG4J   = new LogImplemention("log4j",
        "com.joe.utils.log.log4j2.Log4jOperate");

    /**
     * logback
     */
    public static final LogImplemention LOGBACK = new LogImplemention("logback",
        "com.joe.utils.log.logback.LogbackOperate");

    /**
     * 日志实现名
     */
    private final String                      name;

    /**
     * {@link LogOperate}实现名
     */
    private final String                      converterClass;
}
