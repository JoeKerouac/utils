package com.joe.utils.cluster.lock;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁，所有的锁方法都需要指定最长锁定时长，以避免该实例出错时出现死锁
 * 
 * @author joe
 *
 */
public interface ClusterLock {
	/**
	 * 锁定20分钟，如果超过20分钟仍未主动释放锁那么该锁将被自动释放
	 */
	void lock();

	/**
	 * 锁定一定时间
	 * 
	 * @param leaseTime
	 *            指定锁定时长，超过该时长后会自动释放锁
	 * @param unit
	 *            时长单位
	 */
	void lock(long leaseTime, TimeUnit unit);

	/**
	 * 尝试锁定一定时间
	 * 
	 * @param waitTime
	 *            尝试锁定时间，超过该时间未获取锁会返回false
	 * @param leaseTime
	 *            指定锁定时长，超过该时长后会自动释放锁
	 * @param unit
	 *            时长单位
	 * @return 如果锁定成功返回<code>true</code>，否则返回<code>false</code>
	 * @throws InterruptedException
	 *             如果线程在此方法执行完毕之前中断那么将会抛出该异常
	 */
	boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException;

	/**
	 * 解锁
	 */
	void unlock();

	/**
	 * 判断当前的锁是否处于锁定状态
	 * 
	 * @return 如果当前锁处于锁定状态那么返回<code>true</code>
	 */
	boolean isLocked();
}
