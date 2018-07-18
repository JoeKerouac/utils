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
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.joe.utils.log.log4j2.plugin.Log4j2Helper;
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
@Component
public class SpringPropUtil implements EnvironmentAware {
    private static Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        SpringPropUtil.environment = environment;
        Log4j2Helper.reconfigLog4j2(SpringPropPlugin.class);
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
