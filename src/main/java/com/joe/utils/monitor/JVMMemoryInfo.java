package com.joe.utils.monitor;

/**
 * JVM内存信息
 * 
 * @author qiao9
 *
 */
public class JVMMemoryInfo {
    /**
     * 当前空闲内存，单位MB
     */
    private final double freeMemory;
    /**
     * 虚拟机可用最大内存，单位MB
     */
    private final double maxMemory;
    /**
     * 当前总内存，单位MB
     */
    private final double totalMemory;

    protected JVMMemoryInfo(double freeMemory, double maxMemory, double totalMemory) {
        this.freeMemory = freeMemory;
        this.maxMemory = maxMemory;
        this.totalMemory = totalMemory;
    }

    public double getFreeMemory() {
        return freeMemory;
    }

    public double getMaxMemory() {
        return maxMemory;
    }

    public double getTotalMemory() {
        return totalMemory;
    }
}
