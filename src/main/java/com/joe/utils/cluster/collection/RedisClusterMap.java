package com.joe.utils.cluster.collection;

import org.redisson.api.RMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 基于redis使用redission实现的分布式map
 *
 * @author joe
 */
public class RedisClusterMap<K, V> implements ClusterMap<K , V> {
    private RMap<K, V> rMap;

    public RedisClusterMap(RMap<K, V> rMap) {
        this.rMap = rMap;
    }

    @Override
    public int valueSize(K key) {
        return rMap.valueSize(key);
    }

    @Override
    public V addAndGet(K key, Number delta) {
        return rMap.addAndGet(key, delta);
    }

    @Override
    public Map<K, V> getAll(Set<K> keys) {
        return rMap.getAll(keys);
    }

    @Override
    public long fastRemove(K... keys) {
        return rMap.fastRemove(keys);
    }

    @Override
    public boolean fastPut(K key, V value) {
        return rMap.fastPut(key , value);
    }

    @Override
    public Set<K> readAllKeySet() {
        return rMap.readAllKeySet();
    }

    @Override
    public Collection<V> readAllValues() {
        return rMap.readAllValues();
    }

    @Override
    public Set<Entry<K, V>> readAllEntrySet() {
        return rMap.readAllEntrySet();
    }

    @Override
    public Set<K> keySet() {
        return rMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return rMap.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return rMap.entrySet();
    }

    @Override
    public int size() {
        return rMap.size();
    }

    @Override
    public boolean isEmpty() {
        return rMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return rMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return rMap.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return rMap.get(key);
    }

    @Override
    public V put(K key, V value) {
        return rMap.put(key , value);
    }

    @Override
    public V remove(Object key) {
        return rMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        rMap.putAll(m);
    }

    @Override
    public void clear() {
        rMap.clear();
    }
}
