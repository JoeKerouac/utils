package com.joe.utils.log;

import java.util.function.Function;

/**
 * 日志打印过滤，如果返回false或者null则该日志不会被打印
 *
 * @author JoeKerouac
 * @version 2019年09月18日 11:48
 */
public interface LogFilter extends Function<LogTask, Boolean> {

}
