package com.joe.utils.vm;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * jvm信息
 *
 * @author JoeKerouac
 * @version 2019年08月27日 17:34
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JvmInfo {

    /**
     * jvmName，例如：Java HotSpot(TM) 64-Bit Server VM
     */
    private final String jvmName;

    /**
     * jvm版本，例如：25.202-b08
     */
    private final String jvmVersion;

    /**
     * jvm实现供应商，例如：Oracle Corporation
     */
    private final String jvmVendor;

    /**
     * jvm运行时信息，例如：mixed mode
     */
    private final String jvmInfo;

    /**
     * 获取系统配置
     *
     * @param name 配置名
     * @return 配置value
     */
    private static String getSystemProperty(String name) {
        try {
            return System.getProperty(name);
        } catch (SecurityException e) {
            System.err.println("Caught a SecurityException reading the system property '" + name
                               + "'; the SystemUtil property value will default to null.");

            return null;
        }
    }

    /**
     * 获取JVM信息实例
     * @return JVM信息实例
     */
    public static JvmInfo getInstance() {
        String jvmName = getSystemProperty("java.vm.name");
        String jvmVersion = getSystemProperty("java.vm.version");
        String jvmVendor = getSystemProperty("java.vm.vendor");
        String jvmInfo = getSystemProperty("java.vm.info");
        return new JvmInfo(jvmName, jvmVersion, jvmVendor, jvmInfo);
    }
}
