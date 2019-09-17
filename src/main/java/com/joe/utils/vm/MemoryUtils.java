package com.joe.utils.vm;

import com.joe.utils.common.enums.unit.MemoryUnit;

import java.math.BigDecimal;

/**
 * @author JoeKerouac
 * @version 2019年09月17日 14:00
 */
public class MemoryUtils {
    /**
     * 带单位的数值toString
     * @param num num
     * @param unit unit
     * @return 例如12Byte
     */
    public static String toString(BigDecimal num, MemoryUnit unit) {
        return num.toPlainString() + unit.getDesc();
    }
}
