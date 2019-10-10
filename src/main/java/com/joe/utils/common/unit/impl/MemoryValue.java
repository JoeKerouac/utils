package com.joe.utils.common.unit.impl;

import java.math.BigDecimal;

import com.joe.utils.common.unit.ValueWithUnit;

/**
 * 内存值
 *
 * @author JoeKerouac
 * @version 2019年10月10日 10:10
 */
public class MemoryValue extends ValueWithUnit<MemoryUnitDefinition> {

    private static final long serialVersionUID = -541120886310912512L;

    public MemoryValue(int value, MemoryUnitDefinition memoryUnit) {
        super(value, memoryUnit);
    }

    public MemoryValue(long value, MemoryUnitDefinition memoryUnit) {
        super(value, memoryUnit);
    }

    public MemoryValue(String value, MemoryUnitDefinition memoryUnit) {
        super(value, memoryUnit);
    }

    public MemoryValue(BigDecimal value, MemoryUnitDefinition memoryUnit) {
        super(value, memoryUnit);
    }

}
