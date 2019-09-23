package com.joe.utils.log.exception;

import com.joe.utils.common.string.StringFormater;
import com.joe.utils.exception.UtilsException;

/**
 * 日志异常
 *
 * @author JoeKerouac
 * @version 2019年09月17日 15:45
 */
public class LogException extends UtilsException {

    private static final long serialVersionUID = -3357224634760590669L;

    public LogException(String msgTemp, Object... args) {
        super(StringFormater.simpleFormat(msgTemp, args));
    }
}
