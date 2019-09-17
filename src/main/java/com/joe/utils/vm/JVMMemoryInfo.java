package com.joe.utils.vm;

import java.math.BigDecimal;

import com.joe.utils.common.enums.unit.MemoryUnit;

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
    private final BigDecimal freeMemory;

    /**
     * 虚拟机可用最大内存
     */
    private final BigDecimal maxMemory;

    /**
     * 当前总内存
     */
    private final BigDecimal totalMemory;

    /**
     * 内存单位
     */
    private final MemoryUnit unit;

    /**
     * 获取JVM虚拟机内存信息
     *
     * @param unit 结果单位
     * @return JVM虚拟机内存信息
     */
    public static JVMMemoryInfo getInstance(MemoryUnit unit) {
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        return new JVMMemoryInfo(MemoryUnit.convert(freeMemory, MemoryUnit.BYTE, unit),
            MemoryUnit.convert(maxMemory, MemoryUnit.BYTE, unit),
            MemoryUnit.convert(totalMemory, MemoryUnit.BYTE, unit), unit);
    }

    @Override
    public String toString() {
        return "JVMMemoryInfo{" + "freeMemory=" + MemoryUtils.toString(freeMemory, unit)
               + ", maxMemory=" + MemoryUtils.toString(maxMemory, unit) + ", totalMemory="
               + MemoryUtils.toString(totalMemory, unit) + '}';
    }
}
