package com.joe.utils.collection;

import java.util.*;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年10月09日 下午6:52 JoeKerouac Exp $
 */
public class CollectionUtilTest {

    @Test
    public void innerJoinTest() {
        List<String> list0 = new ArrayList<>(2);
        List<String> list1 = new ArrayList<>(2);
        List<String> list2 = new ArrayList<>(2);
        list0.add("1");
        list0.add("2");
        list1.add("3");
        list1.add("4");
        list2.add("5");
        list2.add("6");

        List<String> expectResult = new ArrayList<>(8);
        expectResult.add("135");
        expectResult.add("136");
        expectResult.add("145");
        expectResult.add("146");
        expectResult.add("235");
        expectResult.add("236");
        expectResult.add("245");
        expectResult.add("246");

        List<List<String>> arg = new ArrayList<>();
        arg.add(list0);
        arg.add(list1);
        arg.add(list2);

        List<String> result = CollectionUtil.innerJoin(arg, (arg0, arg1) -> arg0 + arg1);
        Assert.assertTrue(CollectionUtil.sizeEquals(expectResult, result));
        expectResult.forEach(str -> Assert.assertTrue(result.contains(str)));
    }

    @Test
    public void permutationsTest() {
        List<String> arg = new ArrayList<>(3);
        arg.add("1");
        arg.add("2");
        arg.add("3");

        List<String> expectResult = new ArrayList<>(6);
        expectResult.add("123");
        expectResult.add("132");
        expectResult.add("213");
        expectResult.add("231");
        expectResult.add("312");
        expectResult.add("321");

        List<List<String>> result = CollectionUtil.permutations(arg);
        List<String> resultConvert = result.stream()
            .map(list -> list.stream().reduce((arg0, arg1) -> arg0 + arg1).get())
            .collect(Collectors.toList());

        Assert.assertTrue(CollectionUtil.sizeEquals(expectResult, resultConvert));
        expectResult.forEach(str -> Assert.assertTrue(resultConvert.contains(str)));
    }

    @Test
    public void sizeEqualsTest() {
        Object[] arg0 = null;
        Object[] arg1 = new Object[0];
        Object[] arg2 = new Object[0];
        Object[] arg3 = new Object[1];
        Assert.assertTrue(CollectionUtil.sizeEquals(arg0, arg1));
        Assert.assertTrue(CollectionUtil.sizeEquals(arg1, arg2));
        Assert.assertFalse(CollectionUtil.sizeEquals(arg2, arg3));

        List<Object> list0 = null;
        List<Object> list1 = new ArrayList<>();
        List<Object> list2 = new ArrayList<>();
        List<Object> list3 = new ArrayList<>();
        list3.add("123");
        Assert.assertTrue(CollectionUtil.sizeEquals(list0, list1));
        Assert.assertTrue(CollectionUtil.sizeEquals(list1, list2));
        Assert.assertFalse(CollectionUtil.sizeEquals(list2, list3));

        Map<String, Object> map0 = null;
        Map<String, Object> map1 = new HashMap<>();
        Map<String, Object> map2 = new HashMap<>();
        Map<String, Object> map3 = new HashMap<>();
        map3.put("123", "123");
        Assert.assertTrue(CollectionUtil.sizeEquals(map0, map1));
        Assert.assertTrue(CollectionUtil.sizeEquals(map1, map2));
        Assert.assertFalse(CollectionUtil.sizeEquals(map2, map3));
    }

    @Test
    public void calcStackDeepTest() {
        List<String> list1 = Arrays.asList("a", "b", "c", "d", "e", "f", "g");
        List<String> list2;
        int stackDeep;

        list2 = Arrays.asList("b", "a", "c", "e", "f", "g", "d");
        stackDeep = CollectionUtil.calcStackDeep(list1, list2);
        Assert.assertTrue(stackDeep == 2);
        list2 = Arrays.asList("b", "d", "c", "f", "e", "a", "g");
        stackDeep = CollectionUtil.calcStackDeep(list1, list2);
        Assert.assertTrue(stackDeep == 3);
        list2 = Arrays.asList("d", "c", "e", "g", "f", "b", "a");
        stackDeep = CollectionUtil.calcStackDeep(list1, list2);
        Assert.assertTrue(stackDeep == 4);
        list2 = Arrays.asList("b", "f", "e", "g", "d", "c", "a");
        stackDeep = CollectionUtil.calcStackDeep(list1, list2);
        Assert.assertTrue(stackDeep == 5);
    }

}
