package com.joe.utils.cluster.redis;

import com.joe.utils.cluster.ClusterManager;
import com.joe.utils.cluster.Topic;
import com.joe.utils.cluster.redis.RedisTopic;
import org.redisson.api.RedissonClient;

import java.util.Map;
import java.util.Set;
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

    @Override
    public <K> Set<K> getSet(String name) {
        return redissonClient.getSet(name);
    }

    @Override
    public <M> Topic<M> getTopic(String name) {
        return new RedisTopic<>(redissonClient.getTopic(name));
    }

    @Override
    public void shutdown() {
        redissonClient.shutdown();
    }

    @Override
    protected void finalize() throws Throwable {
        //不应该依赖该方法进行shutdown！！！
        super.finalize();
        redissonClient.shutdown();
    }
}
