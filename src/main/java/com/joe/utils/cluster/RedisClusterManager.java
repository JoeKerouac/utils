package com.joe.utils.cluster;

import org.redisson.api.RedissonClient;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * redis实现的分布式锁管理器
 *
 * @author joe
 */
public class RedisClusterManager implements ClusterManager {
    private RedissonClient redissonClient;

    RedisClusterManager(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public Lock getLock(String name) {
        return redissonClient.getLock(name);
    }

    @Override
    public ReadWriteLock getReadWriteLock(String name) {
        return redissonClient.getReadWriteLock(name);
    }

    @Override
    public <K, V> Map<K, V> getMap(String name) {
        return redissonClient.getMap(name);
    }

    @Override
    public <K, V> ConcurrentMap<K, V> getConcurrentMap(String name) {
        return redissonClient.getMap(name);
    }
}
