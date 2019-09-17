package com.joe.utils.log;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author JoeKerouac
 * @version 2019年09月17日 14:18
 */
@Getter
@AllArgsConstructor
public enum LogLevel {
                      /**
                       * DEBUG级别日志
                       */
                      DEBUG(1, "debug"),

                      /**
                       * INFO级别日志
                       */
                      INFO(1, "info"),

                      /**
                       * WARN级别日志
                       */
                      WARN(1, "warn"),

                      /**
                       * ERROR级别日志
                       */
                      ERROR(1, "error");

    /**
     * 日志级别
     */
    private int    level;

    /**
     * 级别名
     */
    private String name;
}
