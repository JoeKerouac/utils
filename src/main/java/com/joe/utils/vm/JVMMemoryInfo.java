package com.joe.utils.vm;

import com.joe.utils.common.unit.impl.MemoryValue;
import com.joe.utils.common.unit.impl.MemoryUnitDefinition;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * JVM内存信息
 *
 * @author JoeKerouac
 * @version 2019年08月27日 17:29
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JVMMemoryInfo {

    /**
     * 当前空闲内存
     */
    private final MemoryValue freeMemory;

    /**
     * 虚拟机可用最大内存
     */
    private final MemoryValue maxMemory;

    /**
     * 当前总内存
     */
    private final MemoryValue totalMemory;

    /**
     * 获取JVM虚拟机内存信息
     *
     * @param unit 结果单位
     * @return JVM虚拟机内存信息
     */
    public static JVMMemoryInfo getInstance(MemoryUnitDefinition unit) {
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        return new JVMMemoryInfo(MemoryUtils.build(freeMemory, MemoryUnitDefinition.BYTE, unit),
            MemoryUtils.build(maxMemory, MemoryUnitDefinition.BYTE, unit),
            MemoryUtils.build(totalMemory, MemoryUnitDefinition.BYTE, unit));
    }

    @Override
    public String toString() {
        return "JVMMemoryInfo{" + "freeMemory=" + freeMemory + ", maxMemory=" + maxMemory
               + ", totalMemory=" + totalMemory + '}';
    }
}
