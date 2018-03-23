package com.joe.utils.monitor;

/**
 * 类加载信息
 * 
 * @author qiao9
 *
 */
public class ClassLoadInfo {
	/**
	 * 当前加载的class数量
	 */
	private final int nowLoadedClassCount;
	/**
	 * 虚拟机运行以来总共加载的class数量
	 */
	private final long totalLoadedClassCount;
	/**
	 * 卸载的class数量
	 */
	private final long unloadedClassCount;

	protected ClassLoadInfo(int nowLoadedClassCount, long totalLoadedClassCount, long unloadedClassCount) {
		this.nowLoadedClassCount = nowLoadedClassCount;
		this.totalLoadedClassCount = totalLoadedClassCount;
		this.unloadedClassCount = unloadedClassCount;
	}

	public int getNowLoadedClassCount() {
		return nowLoadedClassCount;
	}

	public long getTotalLoadedClassCount() {
		return totalLoadedClassCount;
	}

	public long getUnloadedClassCount() {
		return unloadedClassCount;
	}

}
