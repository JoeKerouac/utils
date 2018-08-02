package com.joe.utils.log.log4j2.plugin.impl;

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

import com.joe.utils.log.log4j2.plugin.Log4j2Helper;

/**
 * log4j2的properties插件，用于使用代码提供log4j配置
 *
 * 使用场景：当log4j2的property需要使用代码动态从环境中获取而无法写死在配置文件中时可以使用该类
 *
 * 使用注意事项：如果调用{@link #reconfigLog4j2(org.apache.logging.log4j.spi.LoggerContext, Map)}
 * 或者{@link #reconfigLog4j2(Map)}方法之前log4j已经被初始化，那么必须在xml配置文件中提供需要的property的默认值用来
 * 初始化，否则log4j2初始化过程中将会报错，稍后在调用reconfigLog4j2方法时会将xml配置文件中提供的property覆盖。
 * <br/><br/>
 * 此方法是采用了反射等黑魔法技术实现，官方提供的还有一种实现，具体的可以参考{@link Interpolator Interpolator}类，但是该
 * 实现需要xml中的properties采用StrLookupName:placeholder的形式，例如mapStrLookup:customLevel，其中mapStrLookup是StrLookup
 * 实现类上的注解@Plugin的name值
 *
 * @author joe
 * @version 2018.07.18 10:55
 */
public class DefaultPropUtil {
    private static Map<String, String> prop;

    /**
     * 使用自定义配置替换log4j中Properties的值（log4j2使用）
     * @param prop 自定义配置
     */
    public static void reconfigLog4j2(Map<String, String> prop) {
        DefaultPropUtil.prop = prop;
        Log4j2Helper.reconfigLog4j2(DefaultPropPlugin.class);
    }

    /**
     * 使用自定义配置替换log4j的指定context中Properties的值（log4j2使用）
     * @param context context，可以使用LogManager.getContext()获取
     * @param prop 自定义配置
     */
    public static void reconfigLog4j2(org.apache.logging.log4j.spi.LoggerContext context,
                                      Map<String, String> prop) {
        DefaultPropUtil.prop = prop;
        Log4j2Helper.reconfigLog4j2(DefaultPropPlugin.class, context);
    }

    @Plugin(name = "properties", category = Node.CATEGORY, printObject = true)
    private static class DefaultPropPlugin {
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

            //这一步是关键
            if (DefaultPropUtil.prop != null) {
                map.putAll(DefaultPropUtil.prop);
            }
            return new Interpolator(new MapLookup(map), config.getPluginPackages());
        }
    }
}
