package com.joe.utils.collection;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.joe.utils.common.Assert;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年10月09日 下午6:52 JoeKerouac Exp $
 */
public class CollectionUtilTest {
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
