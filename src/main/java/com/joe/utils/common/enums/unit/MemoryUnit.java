package com.joe.utils.common.enums.unit;

import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 存储单位
 *
 * @author JoeKerouac
 * @version 2019年08月27日 17:56
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum MemoryUnit {
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
                        GB("GB", 1024L * 1024 * 1024 * 1024, "GB"),

                        /**
                         * TB
                         */
                        TB("TB", 1024L * 1024 * 1024 * 1024 * 1024, "TB");

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

    /**
     * 单位转换，例如
     * @param source 要转换的数值
     * @param srcUnit 要转换的数值的单位
     * @param descUnit 要转换的目标单位
     * @return 转换结果，最终会保留20位小数
     */
    public static BigDecimal convert(long source, MemoryUnit srcUnit, MemoryUnit descUnit) {
        // 保留20位小数
        return new BigDecimal(source).multiply(new BigDecimal(srcUnit.radix))
            .divide(new BigDecimal(descUnit.radix), 20, BigDecimal.ROUND_HALF_EVEN);
    }
}
