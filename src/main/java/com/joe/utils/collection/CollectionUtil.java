package com.joe.utils.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 集合工具类
 *
 * @author joe
 */
public final class CollectionUtil {
    /**
     * 清空map集合
     *
     * @param map 要清空的集合（可以为null）
     * @param <K> map中key的实际类型
     * @param <V> map中value的实际类型
     */
    public static <K, V> void clear(Map<K, V> map) {
        if (map != null) {
            map.clear();
            map = null;
        }
    }

    /**
     * 清空collection集合
     *
     * @param collection 要清空的集合（可以为null）
     * @param <T>        Collection的泛型
     */
    public static <T> void clear(Collection<T> collection) {
        if (collection != null) {
            collection.clear();
            collection = null;
        }
    }

    /**
     * 删除集合中指定位置的数据
     *
     * @param list    集合
     * @param removes 指定位置（需要从小到大排序）
     * @param <T>     List的泛型
     */
    public static <T> void remove(List<T> list, List<Integer> removes) {
        // 不是ArrayList的先转换为ArrayList
        if (!(list instanceof ArrayList)) {
            List<T> newList = new ArrayList<T>(list);
            list = newList;
        }
        int flag = 0;
        // 删除
        for (int i = 0; i < removes.size(); i++) {
            if (flag < removes.get(i)) {
                flag = removes.get(i);
                list.remove(flag - i);
            } else {
                throw new CollectionException("删除集合中多个元素时指针应该按照从小到大的顺序排序");
            }
        }
    }
}
