package com.joe.utils.log.log4j2.plugin.spring;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.lookup.StrLookup;
import org.springframework.core.env.Environment;

/**
 * 使用spring实现的StrLookup
 *
 * @author joe
 * @version 2018.07.18 11:34
 */
public class SpringLookup implements StrLookup {
    /**
     * spring环境信息
     */
    private Environment environment;
    /**
     * 默认StrLookup
     */
    private StrLookup   defaultLookup;

    public SpringLookup(Environment environment, StrLookup defaultLookup) {
        this.environment = environment;
        this.defaultLookup = defaultLookup;
    }

    @Override
    public String lookup(String key) {
        String value = environment.getProperty(key);
        return value == null ? defaultLookup.lookup(key) : value;
    }

    @Override
    public String lookup(LogEvent event, String key) {
        String value;
        //由于log4j2的自动发现机制，此处有可能会为null
        if (event == null && environment != null
            && (value = environment.getProperty(key)) != null) {
            return value;
        }
        return defaultLookup.lookup(event, key);
    }
}
