package com.joe.utils.common.unit;

/**
 * 单位定义接口
 *
 * @author JoeKerouac
 * @version 2019年10月10日 10:17
 */
public interface UnitDefinition {

    /**
     * 获取与最小单位的进制，例如1Kb = 1024 * 1024bit，那么对于KB而言返回1024*1024
     * 
     * @return 与最小单位之间的进制
     */
    long getRadix();

    /**
     * 获取编码
     * 
     * @return 编码
     */
    String getCode();

    /**
     * 获取英文名
     * 
     * @return 英文名
     */
    String getEnglishName();

    /**
     * 获取中文名
     * 
     * @return 中文名
     */
    String getChineseName();
}
