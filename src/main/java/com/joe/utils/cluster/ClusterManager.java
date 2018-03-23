package com.joe.utils.cluster;

import com.joe.utils.cluster.collection.ClusterMap;
import com.joe.utils.cluster.lock.ClusterLock;

/**
 * 分布式管理器
 *
 * @author joe
 */
public interface ClusterManager {
    /**
     * 获取指定名字的lock
     *
     * @param name lock的名字
     * @return 指定名字的lock
     */
    ClusterLock getLock(String name);

    /**
     * 获取指定名字的ClusterMap
     *
     * @param name Map的名字
     * @param <K>  Map的key的类型
     * @param <V>  Map的value的类型
     * @return 指定名字对应的Map
     */
    <K, V> ClusterMap<K, V> getMap(String name);
}
