package com.joe.utils.monitor;

/**
 * 内存区使用情况
 * 
 * <pre>
 *        +----------------------------------------------+
 *        +////////////////           |                  +
 *        +////////////////           |                  +
 *        +----------------------------------------------+
 *
 *        |--------|
 *           init
 *        |---------------|
 *               used
 *        |---------------------------|
 *                  committed
 *        |----------------------------------------------|
 *                            max
 * </pre>
 * 
 * @author qiao9
 *
 */
public class MemoryInfo {
    /**
     * 内存区名字
     */
    private final String name;
    /**
     * 内存区初始化大小
     */
    private final double init;
    /**
     * 内存区已使用大小
     */
    private final double used;
    /**
     * 内存区现在可用大小
     */
    private final double committed;
    /**
     * 内存区最大可用大小
     */
    private final double max;

    public MemoryInfo(String name, double init, double used, double committed, double max) {
        this.name = name;
        this.init = init;
        this.used = used;
        this.committed = committed;
        this.max = max;
    }

    public String getName() {
        return name;
    }

    public double getInit() {
        return init;
    }

    public double getUsed() {
        return used;
    }

    public double getCommitted() {
        return committed;
    }

    public double getMax() {
        return max;
    }

}
