package com.joe.utils.log.log4j2.plugin.spring;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.lookup.Interpolator;
import org.apache.logging.log4j.core.lookup.MapLookup;
import org.apache.logging.log4j.core.lookup.StrLookup;
import org.apache.logging.log4j.spi.LoggerContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import com.joe.utils.log.log4j2.plugin.Log4j2Helper;
import com.joe.utils.log.log4j2.plugin.impl.DefaultPropUtil;

/**
 * {@link DefaultPropUtil DefaultPropUtil}的spring实现
 *
 * 使用方法：spring上下文初始化完毕后调用{@link #reconfigLog4j2(ApplicationContext) reconfigLog4j2}方法或者{@link #reconfigLog4j2(ApplicationContext, LoggerContext) reconfigLog4j2}方法即可
 *
 * @author joe
 * @version 2018.07.18 10:55
 */
public class SpringPropUtil {
    private static Environment environment;

    /**
     * 初始化log4j2日志配置，使用{@link Environment Environment}重新配置log4j2的properties
     * @param context spring应用上下文
     */
    public static void reconfigLog4j2(ApplicationContext context) {
        SpringPropUtil.environment = context.getEnvironment();
        Log4j2Helper.reconfigLog4j2(SpringPropPlugin.class);
    }

    /**
     * 初始化log4j2日志配置，使用{@link Environment Environment}重新配置log4j2的properties
     * @param applicationContext spring应用上下文
     * @param context LoggerContext
     */
    public static void reconfigLog4j2(ApplicationContext applicationContext,
                                      LoggerContext context) {
        SpringPropUtil.environment = applicationContext.getEnvironment();
        Log4j2Helper.reconfigLog4j2(SpringPropPlugin.class, context);
    }

    /**
     * 集成spring的properties插件，用于替换log4j2内置{@link org.apache.logging.log4j.core.config.PropertiesPlugin properties插件}
     *
     * <p>该插件直接用{@link com.joe.utils.log.log4j2.plugin.Log4j2Helper Log4j2Helper}使用反射注入，所以类上不需要{@link org.apache.logging.log4j.core.config.plugins.Plugin @Plugin}注解</p>
     */
    private static class SpringPropPlugin {
        /**
         * Creates the Properties component.
         * @param properties An array of Property elements.
         * @param config The Configuration.
         * @return An Interpolator that includes the configuration properties.
         */
        @PluginFactory
        public static StrLookup configureSubstitutor(@PluginElement("Properties") final Property[] properties,
                                                     @PluginConfiguration final Configuration config) {
            if (properties == null) {
                return new Interpolator(config.getProperties());
            }
            final Map<String, String> map = new HashMap<>(config.getProperties());

            for (final Property prop : properties) {
                map.put(prop.getName(), prop.getValue());
            }

            return new SpringLookup(environment,
                new Interpolator(new MapLookup(map), config.getPluginPackages()));
        }
    }
}
