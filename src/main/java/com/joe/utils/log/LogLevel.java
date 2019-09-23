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
                      DEBUG(0, "debug"),

                      /**
                       * INFO级别日志
                       */
                      INFO(100, "info"),

                      /**
                       * WARN级别日志
                       */
                      WARN(200, "warn"),

                      /**
                       * ERROR级别日志
                       */
                      ERROR(300, "error"),

                      /**
                       * 所有都打印
                       */
                      ALL(400, "all"),

                      /**
                       * 关闭
                       */
                      OFF(Integer.MAX_VALUE, "off");

    /**
     * 日志级别
     */
    private int    code;

    /**
     * 级别名
     */
    private String name;
}
