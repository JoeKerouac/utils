package com.joe.utils.common.unit.impl;

import com.joe.utils.common.unit.UnitDefinition;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 存储单位定义
 *
 * @author JoeKerouac
 * @version 2019年08月27日 17:56
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum MemoryUnitDefinition implements UnitDefinition {
                                                             /**
                                                              * 基本最小单位
                                                              */
                                                             BIT("BIT", 1, "Bit"),

                                                             /**
                                                              * Byte
                                                              */
                                                             BYTE("BYTE", 1024, "Byte"),

                                                             /**
                                                              * KB
                                                              */
                                                             KB("KB", 1024 * 1024, "KB"),

                                                             /**
                                                              * MB
                                                              */
                                                             MB("MB", 1024 * 1024 * 1024, "MB"),

                                                             /**
                                                              * GB
                                                              */
                                                             GB("GB", 1024L * 1024 * 1024 * 1024,
                                                                "GB"),

                                                             /**
                                                              * TB
                                                              */
                                                             TB("TB",
                                                                1024L * 1024 * 1024 * 1024 * 1024,
                                                                "TB");

    /**
     * code
     */
    private final String code;

    /**
     * 最小单位的倍数
     */
    private final long   radix;

    /**
     * 说明
     */
    private final String desc;

    @Override
    public String getEnglishName() {
        return desc;
    }

    @Override
    public String getChineseName() {
        return desc;
    }
}
