package com.joe.utils.reflect.clazz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Enumeration简单实现，非线程安全
 *
 * @author JoeKerouac
 * @version 2019年09月29日 14:20
 */
public class JEnumeration<E> implements Enumeration<E> {

    /**
     * 数据
     */
    private List<E> data;

    /**
     * 指针
     */
    private int point = 0;

    public JEnumeration() {
        this(Collections.emptyList());
    }

    public JEnumeration(List<E> data) {
        this.data = new ArrayList<>(data);
    }

    @Override
    public boolean hasMoreElements() {
        return data.size() > point;
    }

    @Override
    public E nextElement() {
        return data.get(point++);
    }
}
