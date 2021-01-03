package com.joe.utils.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import com.joe.utils.common.exception.ResourceNotFoundException;
import com.joe.utils.common.string.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 资源辅助类
 *
 * @author joe
 * @version 2018.06.12 15:26
 */
@Slf4j
public class ResourceHelper {

    /**
     * 获取resource
     *
     * @param location
     *            resource位置，支持协议参照{@link ResourceProtocol ResourceProtocol}，如果没有指定协议那么默认从classpath中获取
     * @return 对应的resource
     * @throws ResourceNotFoundException
     *             当资源不存在时抛出该异常
     */
    public static InputStream getResource(String location) throws ResourceNotFoundException {
        if (StringUtils.isEmpty(location)) {
            throw new NullPointerException("location must not be null");
        }
        log.debug("获取资源[{}]", location);
        try {
            if (location.startsWith(ResourceProtocol.FILE.protocol)) {
                return new FileInputStream(new File(location.substring(ResourceProtocol.FILE.protocol.length())));
            } else if (location.startsWith(ResourceProtocol.URL.protocol)) {
                return new URL(location.substring(ResourceProtocol.URL.protocol.length())).openStream();
            } else if (location.startsWith(ResourceProtocol.HTTP.protocol)) {
                return new URL(location.substring(ResourceProtocol.HTTP.protocol.length())).openStream();
            } else if (location.startsWith(ResourceProtocol.HTTPS.protocol)) {
                return new URL(location.substring(ResourceProtocol.HTTPS.protocol.length())).openStream();
            } else {
                if (location.startsWith(ResourceProtocol.CLASSPATH.protocol)) {
                    location = location.substring(ResourceProtocol.CLASSPATH.protocol.length());
                }
                return Thread.currentThread().getContextClassLoader().getResourceAsStream(location);
            }
        } catch (Throwable e) {
            log.error("资源[{}]获取失败", location, e);
            throw new ResourceNotFoundException("查找资源[" + location + "]异常", e);
        }
    }

    /**
     * 协议
     */
    public enum ResourceProtocol {
        FILE("file://"), URL("url://"), CLASSPATH("classpath://"), HTTP("http://"), HTTPS("https://");

        /**
         * 协议前缀
         */
        private String protocol;

        ResourceProtocol(String protocol) {
            this.protocol = protocol;
        }

        public String getProtocol() {
            return protocol;
        }
    }
}
