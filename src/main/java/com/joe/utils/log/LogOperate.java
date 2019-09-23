package com.joe.utils.log;

import java.util.List;

import com.joe.utils.log.exception.NoSupportLoggerException;

/**
 * 日志操作组件
 * 
 * @author JoeKerouac
 * @version 2019年09月18日 14:12
 */
public interface LogOperate<LOGGER, LEVEL> {

    /**
     * 将指定实现日志级别转换为当前系统日志级别
     * @param level 日志级别
     * @return 当前系统日志级别
     * @throws NoSupportLoggerException 不支持时抛出该异常
     */
    LogLevel convertToSystem(LEVEL level) throws NoSupportLoggerException;

    /**
     * 将系统日志级别转换为指定日志实现的日志级别
     * @param level 当前系统日志级别
     * @return 指定日志实现的日志级别
     * @throws NoSupportLoggerException 不支持时抛出该异常
     */
    LEVEL convert(LogLevel level) throws NoSupportLoggerException;

    /**
     * 获取指定logger的日志级别
     * @param logger logger
     * @return 日志级别
     * @throws NoSupportLoggerException 不支持时抛出该异常
     */
    LogLevel getLevel(LOGGER logger) throws NoSupportLoggerException;

    /**
     * 设置logger的日志级别
     * @param logger logger
     * @param level level
     * @throws NoSupportLoggerException 不支持时抛出该异常
     */
    void setLevel(LOGGER logger, LogLevel level) throws NoSupportLoggerException;

    /**
     * 获取日志名
     * @param logger logger，不能为空
     * @return 日志名
     * @throws NoSupportLoggerException 不支持时抛出该异常
     */
    String getName(LOGGER logger) throws NoSupportLoggerException;

    /**
     * 获取当前日志系统所有logger
     * @return 当前日志系统所有logger
     * @throws NoSupportLoggerException 不支持时抛出该异常
     */
    List<LOGGER> getAllLogger() throws NoSupportLoggerException;
}
