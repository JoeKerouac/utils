package com.joe.utils.monitor;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.joe.utils.common.IOUtils;

/**
 * 虚拟机管理
 * 
 * @author qiao9
 *
 */
public class Manager {
	/**
	 * 获取class加载信息
	 * 
	 * @return class加载信息
	 */
	public static ClassLoadInfo classLoadManager() {
		ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
		int nowLoadedClassCount = classLoadingMXBean.getLoadedClassCount();
		long totalLoadedClassCount = classLoadingMXBean.getTotalLoadedClassCount();
		long unloadedClassCount = classLoadingMXBean.getUnloadedClassCount();
		ClassLoadInfo info = new ClassLoadInfo(nowLoadedClassCount, totalLoadedClassCount, unloadedClassCount);
		return info;
	}

	/**
	 * 获取JVM虚拟机内存信息
	 * 
	 * @return JVM虚拟机内存信息
	 */
	public static JVMMemoryInfo getJVMMemoryInfo() {
		Runtime runtime = Runtime.getRuntime();
		double freeMemory = (double) runtime.freeMemory() / (1024 * 1024);
		double maxMemory = (double) runtime.maxMemory() / (1024 * 1024);
		double totalMemory = (double) runtime.totalMemory() / (1024 * 1024);
		JVMMemoryInfo info = new JVMMemoryInfo(freeMemory, maxMemory, totalMemory);
		return info;
	}

	/**
	 * 获取各个内存区的使用情况
	 * 
	 * @return 各个内存区的使用情况
	 */
	public static List<MemoryInfo> getMemoryInfo() {
		Map<String, MemoryUsage> map = new TreeMap<String, MemoryUsage>();
		List<MemoryPoolMXBean> memoryPoolMXBeanList = ManagementFactory.getMemoryPoolMXBeans();
		List<MemoryInfo> memoryInfoList = new ArrayList<MemoryInfo>();
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
			double init = (double) memoryUsage.getInit() / (1024 * 1024);
			double max = (double) memoryUsage.getMax() / (1024 * 1024);
			double used = (double) memoryUsage.getUsed() / (1024 * 1024);
			double committed = (double) memoryUsage.getCommitted() / (1024 * 1024);
			MemoryInfo info = new MemoryInfo(name, init, used, committed, max);
			memoryInfoList.add(info);
		}
		return memoryInfoList;
	}

	/**
	 * 获取windows系统已使用总内存（windows系统可用）
	 * 
	 * @return 系统已使用的总内存（单位为M）
	 * @throws Exception
	 *             请参考具体异常信息
	 */
	public static double getSystemMemoryInfo() throws Exception {
		int total = 0;
		Process process = Runtime.getRuntime().exec("tasklist");
		String result = IOUtils.read(process.getInputStream(), Charset.defaultCharset().name());
		String[] results = result.split("\\n");
		for (String str : results) {
			Pattern pattern = Pattern.compile(".*\\s([0-9]{0,},[0-9]+)\\sK.*");
			Matcher matcher = pattern.matcher(str);
			if (matcher.find()) {
				String memorySizeStr = matcher.group(1).replace(",", "");
				int memorySize = Integer.parseInt(memorySizeStr);
				total += memorySize;
			}
		}
		return (double) total / 1024;
	}
}
