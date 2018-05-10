package com.joe.utils.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 常用算法的实现
 *
 * @author joe
 */
public class Algorithm {
    /**
     * 求最长公共子序列（有可能有多种解，该方法只返回其中一种）
     *
     * @param arg0       第一个序列
     * @param arg1       第二个序列
     * @param comparator 比较器
     * @param <T>        数组中数据的实际类型
     * @return 两个序列的公共子序列（返回的是原队列中的倒序） （集合{1 ， 2 ， 3 ， 4}和集合{2 ， 3 ， 4 ，
     * 1}的公共子序列返回值为{4 ， 3 ， 2}）
     */
    public static <T> List<T> lcs(List<T> arg0, List<T> arg1, Comparator<T> comparator) {
        if (arg0 == null || arg1 == null) {
            return Collections.emptyList();
        }
        return lcs(arg0, arg1, comparator, 0, 0);
    }

    /**
     * 求最长公共子序列
     *
     * @param arg0       第一个序列
     * @param arg1       第二个序列
     * @param comparator 比较器
     * @param i          第一个序列的当前位置指针
     * @param j          第二个序列的当前位置指针
     * @param <T>        数组中数据的实际类型
     * @return 两个序列的公共子序列
     */
    private static <T> List<T> lcs(List<T> arg0, List<T> arg1, Comparator<T> comparator, int i, int j) {
        if (arg0.size() == i || arg1.size() == j) {
            return new ArrayList<>(Math.min(arg0.size(), arg1.size()));
        }

        if (comparator.compare(arg0.get(i), arg1.get(j)) == 0) {
            T t = arg0.get(i);
            List<T> list = lcs(arg0, arg1, comparator, i + 1, j + 1);
            list.add(t);
            return list;
        } else {
            List<T> l1 = lcs(arg0, arg1, comparator, i + 1, j);
            List<T> l2 = lcs(arg0, arg1, comparator, i, j + 1);
            return l1.size() > l2.size() ? l1 : l2;
        }
    }

    /**
     * 求最长公共子序列（序列中的元素需要实现Comparable接口）（有可能有多种解，该方法只返回其中一种）
     *
     * @param arg0 第一个序列
     * @param arg1 第二个序列
     * @param <T>  数组中数据的实际类型
     * @return 两个序列的公共子序列（返回的是原队列中的倒序） （集合{1 ， 2 ， 3 ， 4}和集合{2 ， 3 ， 4 ，
     * 1}的公共子序列返回值为{4 ， 3 ， 2}）
     */
    public static <T extends Comparable<T>> List<T> lcs(List<T> arg0, List<T> arg1) {
        if (arg0 == null || arg1 == null) {
            return Collections.emptyList();
        }
        return lcs(arg0, arg1, 0, 0);
    }

    /**
     * 求最长公共子序列（序列中的元素需要实现Comparable接口）
     *
     * @param arg0 第一个序列
     * @param arg1 第二个序列
     * @param i    第一个序列的当前位置指针
     * @param j    第二个序列的当前位置指针
     * @param <T>  数组中数据的实际类型
     * @return 两个序列的公共子序列
     */
    private static <T extends Comparable<T>> List<T> lcs(List<T> arg0, List<T> arg1, int i, int j) {
        if (arg0.size() == i || arg1.size() == j) {
            return new ArrayList<>(Math.min(arg0.size(), arg1.size()));
        }

        if (arg0.get(i) != null && arg1.get(j) != null && arg0.get(i).compareTo(arg1.get(j)) == 0) {
            T t = arg0.get(i);
            List<T> list = lcs(arg0, arg1, i + 1, j + 1);
            list.add(t);
            return list;
        } else {
            List<T> l1 = lcs(arg0, arg1, i + 1, j);
            List<T> l2 = lcs(arg0, arg1, i, j + 1);
            return l1.size() > l2.size() ? l1 : l2;
        }
    }

}
