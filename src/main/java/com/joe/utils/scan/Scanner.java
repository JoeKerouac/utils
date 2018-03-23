package com.joe.utils.scan;

import java.util.List;

/**
 * 扫描器
 * 
 * @author joe
 *
 * @param <T>
 *            要扫描的类型
 * @param <F>
 *            filter类型
 */
public interface Scanner<T, F> {
	/**
	 * 扫描所有
	 * 
	 * @param args
	 *            参数
	 * @return 扫描结果
	 */
	public List<T> scan(Object... args);

	/**
	 * 扫描所有并且用Filter过滤
	 * 
	 * @param filters
	 *            过滤器
	 * @param args
	 *            参数
	 * @return 扫描结果
	 */
	public List<T> scan(List<F> filters, Object... args);
}
