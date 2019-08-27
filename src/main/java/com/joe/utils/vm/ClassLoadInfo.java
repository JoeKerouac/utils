package com.joe.utils.vm;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 类加载信息
 *
 * @author JoeKerouac
 * @version 2019年08月27日 17:29
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassLoadInfo {

    /**
     * 当前加载的class数量
     */
    private final int  nowLoadedClassCount;

    /**
     * 虚拟机运行以来总共加载的class数量
     */
    private final long totalLoadedClassCount;

    /**
     * 卸载的class数量
     */
    private final long unloadedClassCount;

    /**
     * 获取class加载信息
     *
     * @return class加载信息
     */
    public static ClassLoadInfo getInstance() {
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        int nowLoadedClassCount = classLoadingMXBean.getLoadedClassCount();
        long totalLoadedClassCount = classLoadingMXBean.getTotalLoadedClassCount();
        long unloadedClassCount = classLoadingMXBean.getUnloadedClassCount();
        return new ClassLoadInfo(nowLoadedClassCount, totalLoadedClassCount, unloadedClassCount);
    }
}
