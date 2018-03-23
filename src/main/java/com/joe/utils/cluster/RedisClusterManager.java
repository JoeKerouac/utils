package com.joe.utils.cluster;

import com.joe.utils.cluster.collection.ClusterMap;
import com.joe.utils.cluster.collection.RedisClusterMap;
import com.joe.utils.cluster.lock.ClusterLock;
import com.joe.utils.cluster.lock.RedisClusterLock;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * redis实现的分布式锁管理器
 * 
 * @author joe
 *
 */
public class RedisClusterManager implements ClusterManager {
	// 管理redis连接
	private static Map<String, RedissonClient> cache = new HashMap<>();
	private static final Object lock = new Object();
	private RedissonClient redissonClient;

	private RedisClusterManager(RedissonClient redissonClient) {
		this.redissonClient = redissonClient;
	}

	/**
	 * 获取redis实现的分布式锁管理器
	 * 
	 * @param host
	 *            redis的主机地址，例如192.168.1.100
	 * @param port
	 *            redis的端口，例如8080
	 * @return redis实现的分布式锁管理器
	 */
	public static RedisClusterManager getInstance(String host, int port) {
		String add = host + port;
		if (!cache.containsKey(add)) {
			synchronized (lock) {
				if (!cache.containsKey(add)) {
					Config config = new Config();
					config.useSingleServer().setAddress(host + ":" +  port);
					RedissonClient client = Redisson.create(config);
					cache.put(add, client);
				}
			}
		}
		return new RedisClusterManager(cache.get(add));
	}

	@Override
	public <K, V> ClusterMap<K, V> getMap(String name) {
		return new RedisClusterMap<>(redissonClient.getMap(name));
	}

	@Override
	public ClusterLock getLock(String name) {
		return new RedisClusterLock(redissonClient.getLock(name));
	}
}
