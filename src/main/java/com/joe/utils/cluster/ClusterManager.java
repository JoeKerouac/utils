package com.joe.utils.cluster;

import com.joe.utils.cluster.redis.RedisBaseConfig;
import com.joe.utils.cluster.redis.RedisClusterManager;
import com.joe.utils.cluster.redis.RedisClusterManagerFactory;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * 分布式管理器
 *
 * @author joe
 */
public interface ClusterManager {

    /**
     * 获取指定名字的lock，该Lock必须为分布式Lock
     *
     * @param name lock的名字
     * @return 指定名字的lock
     */
    Lock getLock(String name);

    /**
     * 与{@link #getLock(String) getLock
     * }类似，但是要求获取出来的必须是{@link java.util.concurrent.locks.ReadWriteLock ReadWriteLock}的子集
     *
     * @param name lock的名字
     * @return 指定名字的lock
     */
    ReadWriteLock getReadWriteLock(String name);

    /**
     * 获取指定名字的BlockingDeque
     *
     * @param name 名字
     * @param <V>  BlockingDeque中的数据类型
     * @return 指定名字对应的BlockingDeque
     */
    <V> BlockingDeque<V> getBlockingDeque(String name);

    /**
     * 获取指定名字的BlockingQueue
     *
     * @param name 名字
     * @param <V>  BlockingQueue中的数据类型
     * @return 指定名字对应的BlockingQueue
     */
    <V> BlockingQueue<V> getBlockingQueue(String name);

    /**
     * 获取指定名字的list
     *
     * @param name 名字
     * @param <V>  list中的数据类型
     * @return 指定名字对应的list
     */
    <V> List<V> getList(String name);

    /**
     * 获取指定名字的Map，该Map必须能够自动在集群间同步
     *
     * @param name Map的名字
     * @param <K>  Map的key的类型
     * @param <V>  Map的value的类型
     * @return 指定名字对应的Map
     */
    <K, V> Map<K, V> getMap(String name);

    /**
     * 与{@link #getMap(String) getMap}类似，但是要求获取出来的Map为{@link java.util.concurrent.ConcurrentMap ConcurrentMap}
     * 的子集
     *
     * @param name Map的名字
     * @param <K>  Map的key的类型
     * @param <V>  Map的value的类型
     * @return 指定名字对应的Map
     */
    <K, V> ConcurrentMap<K, V> getConcurrentMap(String name);

    /**
     * 获取一个分布式Set
     *
     * @param name Set的名字
     * @param <K>  Set中数据类型
     * @return Set
     */
    <K> Set<K> getSet(String name);

    /**
     * 获取topic
     *
     * @param name topic name
     * @param <M>  topic中message的类型
     * @return 对应的topic
     */
    <M> Topic<M> getTopic(String name);

    /**
     * 关闭分布式管理器
     */
    void shutdown();

    /**
     * 获取redis实现的分布式管理器
     *
     * @param host redis的主机地址，例如192.168.1.100
     * @param port redis的端口，例如8080
     * @return redis实现的分布式锁管理器
     */
    static RedisClusterManager getInstance(String host, int port) throws MalformedURLException {
        return RedisClusterManagerFactory.getInstance(host, port);
    }

    /**
     * 获取redis实现的分布式管理器
     *
     * @param host     redis的主机地址，例如192.168.1.100
     * @param port     redis的端口，例如8080
     * @param password 密码
     * @return redis实现的分布式锁管理器
     */
    static RedisClusterManager getInstance(String host, int port, String password) throws MalformedURLException {
        return RedisClusterManagerFactory.getInstance(host, port, password);
    }

    /**
     * 获取redis实现的分布式管理器
     *
     * @param redisBaseConfig redis配置
     * @return 分布式管理器
     */
    static RedisClusterManager getInstance(RedisBaseConfig redisBaseConfig) {
        return RedisClusterManagerFactory.getInstance(redisBaseConfig);
    }
}
