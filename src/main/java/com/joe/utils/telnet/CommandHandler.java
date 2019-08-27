package com.joe.utils.telnet;

import java.util.function.Function;

/**
 * telnet命令处理器
 *
 * @author joe
 * @version 2018.07.19 18:30
 */
public interface CommandHandler extends Function<String, String> {
}
