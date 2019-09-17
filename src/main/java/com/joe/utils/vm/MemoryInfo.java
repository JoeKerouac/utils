package com.joe.utils.vm;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.joe.utils.common.enums.unit.MemoryUnit;

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
    private final String     name;

    /**
     * 内存区初始化大小
     */
    private final BigDecimal init;

    /**
     * 内存区已使用大小
     */
    private final BigDecimal used;

    /**
     * 内存区现在可用大小
     */
    private final BigDecimal committed;

    /**
     * 内存区最大可用大小
     */
    private final BigDecimal max;

    /**
     * 内存单位
     */
    private final MemoryUnit unit;

    /**
     * 获取各个内存区的使用情况
     *
     * @param unit 结果单位
     * @return 各个内存区的使用情况
     */
    public static List<MemoryInfo> getMemoryInfos(MemoryUnit unit) {
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

            MemoryInfo info = new MemoryInfo(name, MemoryUnit.convert(init, MemoryUnit.BYTE, unit),
                MemoryUnit.convert(used, MemoryUnit.BYTE, unit),
                MemoryUnit.convert(committed, MemoryUnit.BYTE, unit),
                MemoryUnit.convert(max, MemoryUnit.BYTE, unit), unit);
            memoryInfoList.add(info);
        }
        return memoryInfoList;
    }

    @Override
    public String toString() {
        return "MemoryInfo{" + "name='" + name + '\'' + ", init=" + MemoryUtils.toString(init, unit)
               + ", used=" + MemoryUtils.toString(used, unit) + ", committed="
               + MemoryUtils.toString(committed, unit) + ", max=" + MemoryUtils.toString(max, unit)
               + '}';
    }
}
