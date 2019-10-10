package com.joe.utils.common.unit;

import com.joe.utils.common.unit.impl.MemoryValue;
import com.joe.utils.common.unit.impl.MemoryUnitDefinition;
import org.junit.Assert;
import org.junit.Test;

/**
 * 单位转换测试
 *
 * @author JoeKerouac
 * @version 2019年08月27日 18:56
 */
public class ValueWithUnitTest {

    @Test
    public void doChangeUnitTest() {
        // 更改单位
        MemoryValue memoryValue = new MemoryValue(1024, MemoryUnitDefinition.BYTE);
        memoryValue.changeUnit(MemoryUnitDefinition.KB);

        Assert.assertEquals(memoryValue.intValue(), 1);
        Assert.assertEquals(memoryValue.longValue(), 1);
    }

    @Test
    public void doCompareToTest() {
        // 不同单位的两个值比较
        MemoryValue value1 = new MemoryValue(1024, MemoryUnitDefinition.BYTE);
        MemoryValue value2 = new MemoryValue(1, MemoryUnitDefinition.KB);
        Assert.assertTrue(!value1.equals(value2));
        Assert.assertEquals(value1.compareTo(value2), 0);
    }

    @Test
    public void doAddTest() {
        // 不同单位的两个值相加
        MemoryValue value1 = new MemoryValue(1024, MemoryUnitDefinition.BYTE);
        MemoryValue value2 = new MemoryValue(1, MemoryUnitDefinition.KB);
        value1.add(value2);

        Assert.assertEquals(value1.getValue().intValue(), 2048);
    }
}
