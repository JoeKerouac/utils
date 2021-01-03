package com.joe.utils.vm;

import com.joe.utils.common.unit.impl.MemoryValue;
import com.joe.utils.common.unit.impl.MemoryUnitDefinition;

/**
 * @author JoeKerouac
 * @version 2019年09月17日 14:00
 */
public class MemoryUtils {

    /**
     * 构建JMemoryValue，并且做一次单位转换
     * 
     * @param value
     *            基本值
     * @param srcUnit
     *            单位
     * @param destUnit
     *            最终结果需要的单位
     * @return JMemoryValue
     */
    public static MemoryValue build(long value, MemoryUnitDefinition srcUnit, MemoryUnitDefinition destUnit) {
        MemoryValue freeMemoryValue = new MemoryValue(value, srcUnit);
        freeMemoryValue.changeUnit(destUnit);
        return freeMemoryValue;
    }
}
