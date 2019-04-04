package com.joe.utils.collection;

import java.util.*;

import org.junit.Test;

import com.joe.utils.common.Assert;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年10月09日 下午6:52 JoeKerouac Exp $
 */
public class CollectionUtilTest {

    @Test
    public void sizeEqualsTest() {
        Object[] arg0 = null;
        Object[] arg1 = new Object[0];
        Object[] arg2 = new Object[0];
        Object[] arg3 = new Object[1];
        Assert.isTrue(CollectionUtil.sizeEquals(arg0, arg1));
        Assert.isTrue(CollectionUtil.sizeEquals(arg1, arg2));
        Assert.isFalse(CollectionUtil.sizeEquals(arg2, arg3));


        List<Object> list0 = null;
        List<Object> list1 = new ArrayList<>();
        List<Object> list2 = new ArrayList<>();
        List<Object> list3 = new ArrayList<>();
        list3.add("123");
        Assert.isTrue(CollectionUtil.sizeEquals(list0, list1));
        Assert.isTrue(CollectionUtil.sizeEquals(list1, list2));
        Assert.isFalse(CollectionUtil.sizeEquals(list2, list3));

        Map<String, Object> map0 = null;
        Map<String, Object> map1 = new HashMap<>();
        Map<String, Object> map2 = new HashMap<>();
        Map<String, Object> map3 = new HashMap<>();
        map3.put("123", "123");
        Assert.isTrue(CollectionUtil.sizeEquals(map0, map1));
        Assert.isTrue(CollectionUtil.sizeEquals(map1, map2));
        Assert.isFalse(CollectionUtil.sizeEquals(map2, map3));
    }

    @Test
    public void calcStackDeepTest() {
        List<String> list1 = Arrays.asList("a", "b", "c", "d", "e", "f", "g");
        List<String> list2;
        int stackDeep;

        list2 = Arrays.asList("b", "a", "c", "e", "f", "g", "d");
        stackDeep = CollectionUtil.calcStackDeep(list1, list2);
        Assert.isTrue(stackDeep == 2);
        list2 = Arrays.asList("b", "d", "c", "f", "e", "a", "g");
        stackDeep = CollectionUtil.calcStackDeep(list1, list2);
        Assert.isTrue(stackDeep == 3);
        list2 = Arrays.asList("d", "c", "e", "g", "f", "b", "a");
        stackDeep = CollectionUtil.calcStackDeep(list1, list2);
        Assert.isTrue(stackDeep == 4);
        list2 = Arrays.asList("b", "f", "e", "g", "d", "c", "a");
        stackDeep = CollectionUtil.calcStackDeep(list1, list2);
        Assert.isTrue(stackDeep == 5);
    }

}
