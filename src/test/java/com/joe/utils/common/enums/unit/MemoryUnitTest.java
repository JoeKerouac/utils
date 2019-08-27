package com.joe.utils.common.enums.unit;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

/**
 * 单位转换测试
 *
 * @author JoeKerouac
 * @version 2019年08月27日 18:56
 */
public class MemoryUnitTest {

    @Test
    public void doTest() {
        MemoryUnit[] memoryUnits = MemoryUnit.values();
        long memory = 1024;
        for (int i = 0; i < memoryUnits.length - 1; i++) {
            MemoryUnit srcUnit = memoryUnits[i];
            MemoryUnit destUnit = memoryUnits[i + 1];
            BigDecimal bigDecimal = MemoryUnit.convert(memory, srcUnit, destUnit);
            Assert.assertEquals(bigDecimal.intValue(), 1);
        }
    }
}
