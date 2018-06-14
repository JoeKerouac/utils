package com.joe.utils.collection;


import com.joe.utils.math.MathUtil;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.BiFunction;

/**
 * 集合工具类
 *
 * @author joe
 */
public final class CollectionUtil {

    /**
     * 安全判断集合是否为空
     *
     * @param collection collection集合
     * @return 返回true表示空
     */
    public static boolean safeIsEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 安全判断集合是否为空
     *
     * @param map map集合
     * @return 返回true表示空
     */
    public static boolean safeIsEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 对ByteBuffer扩容（扩容后position为buffer.capacity()，mark为-1，limit为新buffer的capacity）
     *
     * @param buffer 原ByteBuffer
     * @param size   要增加的大小
     * @return 扩容后的ByteBuffer，扩容后的capacity等于原buffer的capacity + size
     */
    public static ByteBuffer grow(ByteBuffer buffer, int size) {
        return grow(buffer, 0, buffer.capacity(), size);
    }

    /**
     * 对ByteBuffer扩容（扩容后position为从原缓冲区复制的数据的长度，即len参数，mark为-1，limit为新buffer的capacity）
     *
     * @param buffer 原ByteBuffer
     * @param offset 要复制原缓冲区数据的起始位置
     * @param len    要复制的数据的长度
     * @param size   要增加的大小
     * @return 扩容后的ByteBuffer，扩容后的capacity等于原buffer的capacity + size，扩容后的数据等于原来缓冲区offset开始长度为len的数据
     */
    public static ByteBuffer grow(ByteBuffer buffer, int offset, int len, int size) {
        if ((size | offset | len) <= 0) {
            throw new IllegalArgumentException("参数错误");
        }

        if (offset >= buffer.capacity()) {
            throw new ArrayIndexOutOfBoundsException("offset必须小于原缓冲区的capacity");
        }

        if (offset + len > buffer.capacity()) {
            len = buffer.capacity() - offset;
        }

        if (buffer.capacity() == Integer.MAX_VALUE) {
            throw new OutOfMemoryError("当前缓冲区已经达到最大，无法继续扩容");
        }

        //确定扩容后的大小
        int newSize = buffer.capacity() + size;

        //检查大小
        if (newSize < 0) {
            throw new OutOfMemoryError("buffer原大小为：" + buffer.capacity() + "；要扩容的大小为：" + size + "；大于int最大值，请调整size");
        }

        //申请新ByteBuffer，类型和原来的一致，原来是direct就还申请direct类型的，原来是heap就还申请heap类型的
        ByteBuffer newBuffer;
        byte[] data;
        if (buffer.isDirect()) {
            newBuffer = ByteBuffer.allocateDirect(newSize);
            //从0开始copy，将整个数据copy过去；注：Direct类型的ByteBuffer不能使用array获得
            data = new byte[len];
            buffer.position(offset);
            buffer.get(data);
            newBuffer.put(data);
        } else {
            newBuffer = ByteBuffer.allocate(newSize);
            //获取数据
            data = buffer.array();
            //恢复数据
            newBuffer.put(data, offset, len);
        }

        newBuffer.position(len);
        return newBuffer;
    }

    /**
     * 将多个数组内连接
     *
     * @param list     多个数组集合，不能为空
     * @param function 连接函数，使用该函数连接，不能为空
     * @param <T>      数组内数据类型
     * @return 内连接结果，例如原数组为[[1,2],[3,4],[5,6]]，内连接结果为[135,136,145,146,235,236,245,246]
     */
    public static <T> List<T> innerJoin(List<List<T>> list, BiFunction<T, T, T> function) {
        if (list == null || function == null) {
            throw new NullPointerException("list or function must not be null");
        }
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        if (list.size() == 1) {
            return list.get(0);
        }

        List<List<T>> result = innerJoin(list.get(0), list.get(1), function);
        if (list.size() == 2) {
            return merge(result);
        } else {
            for (int i = 2; i < list.size(); i++) {
                List<T> l1 = list.get(i);
                List<List<T>> temp = new ArrayList<>();
                result.stream().forEach(l -> temp.addAll(innerJoin(l, l1, function)));
                result = temp;
            }
            return merge(result);
        }
    }

    /**
     * 合并数组，将多个数组中的内容合并到一个数组，例如参数数组为[[1,2],[3,4]]，合并后为[1,2,3,4]
     *
     * @param list 要合并的数组
     * @param <T>  数据类型
     * @return 合并结果
     */
    public static <T> List<T> merge(List<List<T>> list) {
        List<T> result = new ArrayList<>();
        list.stream().forEach(result::addAll);
        return result;
    }

    /**
     * 将两个数组内连接，原数组为[[1,2],[3,4]]，内连接后为[[13, 14], [23, 24]]
     *
     * @param l1       第一个数组
     * @param l2       第二个数组
     * @param function 连接函数
     * @param <T>      数据类型
     * @return 连接结果
     */
    public static <T> List<List<T>> innerJoin(List<T> l1, List<T> l2, BiFunction<T, T, T> function) {
        if (l1 == null || l2 == null) {
            throw new NullPointerException("inner join arrays must not be null");
        }

        if (l1.isEmpty() && l2.isEmpty()) {
            return Collections.emptyList();
        } else if (l1.isEmpty()) {
            return Collections.singletonList(l2);
        } else if (l2.isEmpty()) {
            return Collections.singletonList(l1);
        }

        List<List<T>> result = new ArrayList<>(l1.size() * l2.size());
        l1.stream().forEach(t1 -> {
            List<T> l = new ArrayList<>();
            l2.stream().forEach(t2 -> l.add(function.apply(t1, t2)));
            result.add(l);
        });
        return result;
    }

    /**
     * 将集合中的数据全排列
     *
     * @param list 集合数据
     * @param <T>  数据类型
     * @return 全排列结果，例如传入[1,2]，返回[[1,2], [2,1]]
     */
    public static <T> List<List<T>> permutations(List<T> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        long size = MathUtil.factorial(list.size());
        if (size > Integer.MAX_VALUE) {
            throw new OutOfMemoryError("全排列结果集大小为[" + size + "]，超过数组能容纳的最大结果");
        }
        List<List<T>> result = new ArrayList<>((int) size);
        permutations(result, list, 0);
        return null;
    }

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
            list = new ArrayList<>(list);
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

    /**
     * 计算全排列
     *
     * @param result 全排列结果集
     * @param args   要进行全排列的队列
     * @param index  全排列开始位置，例如如果index等于3则表示从下标3位置开始往后的所有数据进行全排列
     * @param <T>    要全排列的数据的类型
     */
    public static <T> void permutations(List<List<T>> result, List<T> args, int index) {
        if (index == args.size() - 2) {
            List<T> temp1 = new ArrayList<>(args.size());
            temp1.addAll(args);
            Collections.swap(temp1, index, index + 1);
            result.add(temp1);
            List<T> temp2 = new ArrayList<>(args.size());
            temp2.addAll(args);
            result.add(temp2);
            return;
        }
        permutations(result, args, index + 1);
        for (int i = index; i < args.size() - 1; i++) {
            List<T> temp = new ArrayList<>(args.size());
            temp.addAll(args);
            Collections.swap(temp, index, i + 1);
            permutations(result, temp, index + 1);
        }
    }

}
