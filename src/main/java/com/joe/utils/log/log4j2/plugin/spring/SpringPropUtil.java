package com.joe.utils.log.log4j2.plugin.spring;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.lookup.Interpolator;
import org.apache.logging.log4j.core.lookup.MapLookup;
import org.apache.logging.log4j.core.lookup.StrLookup;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import com.joe.utils.log.log4j2.plugin.impl.DefaultPropUtil;

/**
 * {@link DefaultPropUtil DefaultPropUtil}的spring实现，只支持slf4j，不支持
 * 单独的log4j2
 *
 * 使用方法：只需要将该类加入spring的bean扫描路径即可
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
    public static void init(ApplicationContext context) {
        SpringPropUtil.environment = context.getEnvironment();
    }

    @Plugin(name = "properties", category = Node.CATEGORY, printObject = true)
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
