package com.joe.utils.log.log4j2.plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;

/**
 * 公共工具类
 *
 * @author joe
 * @version 2018.07.18 14:07
 */
public class Log4j2Helper {

    /**
     * 使用自定义配置替换log4j中Properties的值（log4j2使用）
     * @param pluginClazz 插件class
     */
    public static void reconfigLog4j2(Class<?> pluginClazz) {
        //使用slf4j需要更改该context
        reconfigLog4j2(pluginClazz, LogManager.getContext(false));
        //直接使用log4j2需要更改该context
        reconfigLog4j2(pluginClazz, LogManager.getContext(true));
    }

    /**
     * 使用自定义配置替换log4j的指定context中Properties的值（log4j2使用）
     * @param pluginClazz 插件class
     * @param context 要替换配置的context，可以使用LogManager.getContext()获取
     */
    public static void reconfigLog4j2(Class<?> pluginClazz,
                                      org.apache.logging.log4j.spi.LoggerContext context) {
        PluginManager.addPackage(pluginClazz.getPackage().getName());

        LoggerContext loggerContext = (LoggerContext) context;
        loggerContext.reconfigure();
    }
}
