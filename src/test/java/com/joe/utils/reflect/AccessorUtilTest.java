package com.joe.utils.reflect;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年04月03日 17:19 JoeKerouac Exp $
 */
public class AccessorUtilTest {

    @Test
    public void otherTest() {
        Assert.assertTrue(
            AccessorUtil.isFinal(ReflectUtil.getField(ReflectUtilTest.AbstractUser.class, "age")));
        Assert.assertFalse(
            AccessorUtil.isFinal(ReflectUtil.getField(ReflectUtilTest.User.class, "name")));

        Assert.assertTrue(
            AccessorUtil.isStatic(ReflectUtil.getField(ReflectUtilTest.User.class, "ID")));
        Assert.assertFalse(
            AccessorUtil.isStatic(ReflectUtil.getField(ReflectUtilTest.User.class, "name")));

        Assert.assertTrue(
            AccessorUtil.isFinal(ReflectUtil.getMethod(ReflectUtilTest.User.class, "say")));
        Assert.assertFalse(
            AccessorUtil.isFinal(ReflectUtil.getMethod(ReflectUtilTest.User.class, "getName")));

        Assert.assertTrue(
            AccessorUtil.isProtected(ReflectUtil.getMethod(ReflectUtilTest.User.class, "getName")));
        Assert.assertFalse(
            AccessorUtil.isProtected(ReflectUtil.getMethod(ReflectUtilTest.User.class, "say")));

        Assert.assertTrue(
            AccessorUtil.isStatic(ReflectUtil.getMethod(ReflectUtilTest.User.class, "getId")));
        Assert.assertFalse(
            AccessorUtil.isStatic(ReflectUtil.getMethod(ReflectUtilTest.User.class, "say")));

        Assert.assertTrue(
            AccessorUtil.isAbstract(ReflectUtil.getMethod(ReflectUtilTest.Say.class, "say")));
        Assert.assertFalse(
            AccessorUtil.isAbstract(ReflectUtil.getMethod(ReflectUtilTest.User.class, "say")));
    }
}
