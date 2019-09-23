package com.joe.utils.log.exception;

import com.joe.utils.common.string.StringFormater;

/**
 * 不支持的日志实现
 *
 * @author JoeKerouac
 * @version 2019年09月19日 18:00
 */
public class NoSupportLoggerException extends LogException {

    private static final long serialVersionUID = -1588162187383236501L;

    public NoSupportLoggerException(String msgTemp, Object... args) {
        super(StringFormater.simpleFormat(msgTemp, args));
    }

}
