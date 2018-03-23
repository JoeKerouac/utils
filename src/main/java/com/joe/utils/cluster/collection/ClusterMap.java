package com.joe.utils.cluster.collection;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 分布式MAP
 *
 * @param <K> map的key的实际类型
 * @param <V> map的value的实际类型
 * @author joe
 */
public interface ClusterMap<K, V> extends Map<K, V> {

    /**
     * 返回指定key对应的数据的大小，单位为byte
     *
     * @param key 指定的key
     * @return key对应的数据的大小，单位为byte
     */
    int valueSize(K key);

    /**
     * 将当前的key对应的value增加指定的值（原子操作），只有在key对应的值类型为number时才有效
     *
     * @param key   指定的key
     * @param delta 要增加的大小
     * @return 更新后的value
     */
    V addAndGet(K key, Number delta);

    /**
     * 获取指定key集合对应的key、value集合（即将指定key集合对应的所有值取出构建一个新的map）
     *
     * @param keys 指定的key集合
     * @return 指定key集合对应的key、value集合，返回的map是一个新Map对象，对其中的数据操作不会同步到缓存
     */
    Map<K, V> getAll(Set<K> keys);

    /**
     * 快速删除指定的key集合，该操作是一个异步操作，比remove操作快，但是不会返回被删除的key集合对应的value
     *
     * @param keys 指定的key集合
     * @return 将要删除的个数（不一定等于传入的key集合的size，仅是实际要删除的key的数量）
     */
    long fastRemove(K... keys);

    /**
     * 快速放入，与{@link #fastRemove(Object[])}类似
     *
     * @param key   要放入的key
     * @param value 要放入的value
     * @return 如果key已经存在那么返回false
     */
    boolean fastPut(K key, V value);

    /**
     * 获取所有key的集合
     *
     * @return keys
     */
    Set<K> readAllKeySet();

    /**
     * 获取所有value的集合
     *
     * @return values
     */
    Collection<V> readAllValues();

    /**
     * 获取所有Entry的集合
     *
     * @return entries
     */
    Set<Map.Entry<K, V>> readAllEntrySet();

    /**
     * 返回所有key的集合（不会使用{@link #readAllKeySet()}获取）
     */
    @Override
    Set<K> keySet();

    /**
     * 参照{@link #keySet()}
     */
    @Override
    Collection<V> values();

    /**
     * 参照{@link #keySet()}
     */
    @Override
    Set<java.util.Map.Entry<K, V>> entrySet();
}
