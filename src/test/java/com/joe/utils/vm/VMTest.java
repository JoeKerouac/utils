package com.joe.utils.vm;

import org.junit.Assert;
import org.junit.Test;

import com.joe.utils.common.enums.unit.MemoryUnit;

public class VMTest {

    @Test
    public void doTestHostInfo() {
        Assert.assertNotNull(HostInfo.getInstance());
    }

    @Test
    public void doTestJvmInfo() {
        Assert.assertNotNull(JvmInfo.getInstance());
    }

    @Test
    public void doTestJVMMemoryInfo() {
        Assert.assertNotNull(JVMMemoryInfo.getInstance(MemoryUnit.MB));
    }

    @Test
    public void doTestClassLoadInfo() {
        Assert.assertNotNull(ClassLoadInfo.getInstance());
    }

    @Test
    public void doTestMemoryInfo() {
        Assert.assertNotNull(MemoryInfo.getMemoryInfos(MemoryUnit.MB));
    }
}
