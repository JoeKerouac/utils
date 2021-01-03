package com.joe.utils.vm;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.joe.utils.common.unit.impl.MemoryValue;
import com.joe.utils.common.unit.impl.MemoryUnitDefinition;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
 * @author JoeKerouac
 * @version 2019年08月27日 17:34
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemoryInfo {

    /**
     * 内存区名字
     */
    private final String name;

    /**
     * 内存区初始化大小
     */
    private final MemoryValue init;

    /**
     * 内存区已使用大小
     */
    private final MemoryValue used;

    /**
     * 内存区现在可用大小
     */
    private final MemoryValue committed;

    /**
     * 内存区最大可用大小
     */
    private final MemoryValue max;

    /**
     * 获取各个内存区的使用情况
     *
     * @param unit
     *            结果单位
     * @return 各个内存区的使用情况
     */
    public static List<MemoryInfo> getMemoryInfos(MemoryUnitDefinition unit) {
        Map<String, MemoryUsage> map = new TreeMap<>();
        List<MemoryPoolMXBean> memoryPoolMXBeanList = ManagementFactory.getMemoryPoolMXBeans();
        List<MemoryInfo> memoryInfoList = new ArrayList<>();
        for (MemoryPoolMXBean bean : memoryPoolMXBeanList) {
            String name = bean.getName();
            MemoryUsage memoryUsage = bean.getUsage();
            map.put(name, memoryUsage);
        }

        MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
        // 堆内存总使用情况
        map.put("heapMemory", bean.getHeapMemoryUsage());
        // 非堆内存使用情况
        map.put("nonHeapMemory", bean.getNonHeapMemoryUsage());

        for (Map.Entry<String, MemoryUsage> entity : map.entrySet()) {
            String name = entity.getKey();
            MemoryUsage memoryUsage = entity.getValue();
            long init = memoryUsage.getInit();
            long max = memoryUsage.getMax();
            long used = memoryUsage.getUsed();
            long committed = memoryUsage.getCommitted();

            MemoryInfo info = new MemoryInfo(name, MemoryUtils.build(init, MemoryUnitDefinition.BYTE, unit),
                MemoryUtils.build(used, MemoryUnitDefinition.BYTE, unit),
                MemoryUtils.build(committed, MemoryUnitDefinition.BYTE, unit),
                MemoryUtils.build(max, MemoryUnitDefinition.BYTE, unit));
            memoryInfoList.add(info);
        }
        return memoryInfoList;
    }

    @Override
    public String toString() {
        return "MemoryInfo{" + "name='" + name + '\'' + ", init=" + init + ", used=" + used + ", committed=" + committed
            + ", max=" + max + '}';
    }
}
