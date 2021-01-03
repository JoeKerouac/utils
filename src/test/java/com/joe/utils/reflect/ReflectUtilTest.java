package com.joe.utils.reflect;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年04月03日 14:15 JoeKerouac Exp $
 */
public class ReflectUtilTest {

    @Test
    public void getFieldValueTest() {
        User user = new User();
        user.setName("123");
        String name = ReflectUtil.getFieldValue(user, "name");
        Assert.assertEquals(name, user.getName());

        Assert.assertEquals(User.ID, ReflectUtil.getFieldValue(User.class, "ID"));
    }

    @Test
    public void getConstructorTest() {
        Constructor<User> constructor = ReflectUtil.getConstructor(User.class, null);
        Assert.assertNotNull(constructor);
    }

    @Test
    public void invokeTest() throws NoSuchMethodException {
        String name = "123";
        User user = new User();
        user.setName(name);
        Assert.assertEquals(ReflectUtil.invoke(user, "getName", null), name);
        ReflectUtil.invoke(user, "setName", new Class[] {String.class}, "234");
        Assert.assertEquals(user.getName(), "234");

        Method method = User.class.getDeclaredMethod("getName");
        Assert.assertEquals(ReflectUtil.invoke(user, method), "234");
    }

    @Test
    public void getAllMethodTest() {
        Assert.assertEquals(ReflectUtil.getAllMethod(User.class).size(), 5);
    }

    @Test
    public void filterTest() throws NoSuchMethodException {
        List<Method> list = new ArrayList<>(2);
        list.add(User.class.getDeclaredMethod("say"));
        list.add(Say.class.getDeclaredMethod("say"));
        Assert.assertEquals(User.class.getDeclaredMethod("say"), ReflectUtil.filter(list, User.class));
    }

    @Test
    public void getMethodTest() throws NoSuchMethodException {
        // 正常获取方法
        Assert.assertEquals(User.class.getDeclaredMethod("say"), ReflectUtil.getMethod(User.class, "say"));
        Assert.assertEquals(User.class.getDeclaredMethod("getName"), ReflectUtil.getMethod(User.class, "getName"));
        Assert.assertEquals(User.class.getDeclaredMethod("setName", String.class),
            ReflectUtil.getMethod(User.class, "setName", String.class));

        // 验证无法获取未覆写的父类方法
        ReflectException exception = null;
        try {
            Assert.assertNotNull(ReflectUtil.getMethod(User.class, "hello"));
        } catch (ReflectException e) {
            exception = e;
        }
        Assert.assertNotNull(exception);
    }

    @Test
    public void execMethodTest() {
        String name = "123";
        User user = new User();
        user.setName(name);
        Method getName = ReflectUtil.getMethod(User.class, "getName");
        Assert.assertEquals(user.getName(), ReflectUtil.execMethod(getName, user));

        Method getId = ReflectUtil.getMethod(User.class, "getId");
        Assert.assertEquals(User.ID, ReflectUtil.execMethod(getId, user));
        Assert.assertEquals(User.ID, ReflectUtil.execMethod(getId, User.class));
    }

    @Test
    public void getAllAnnotationPresentClassTest() {
        List<Class<?>> list = ReflectUtil.getAllAnnotationPresentClass(CustomAnnotation.class, "com.joe.utils.reflect");
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(Say.class, list.get(0));
    }

    @Test
    public void getAllAnnotationPresentMethodTest() {
        List<Method> list = ReflectUtil.getAllAnnotationPresentMethod(User.class, CustomAnnotation.class);
        Assert.assertEquals(2, list.size());
        Assert.assertFalse(list.contains(ReflectUtil.getMethod(User.class, "say")));
    }

    @Test
    public void getFieldTest() {
        Assert.assertEquals(4, ReflectUtil.getAllFields(User.class).length);

        Assert.assertNotNull(ReflectUtil.getField(User.class, "name"));
        Assert.assertNotNull(ReflectUtil.getField(User.class, "ID"));
        Exception e = null;
        try {
            ReflectUtil.getField(User.class, "age");
        } catch (Exception e1) {
            e = e1;
        }
        Assert.assertNotNull(e);
    }

    public static class User extends AbstractUser implements Say {
        private static final String ID = "user";

        private String name;

        @Override
        public final String say() {
            return name;
        }

        @CustomAnnotation
        protected String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public static String getId() {
            return ID;
        }
    }

    public static abstract class AbstractUser {
        private String name;
        private final int age = 123;
    }

    @CustomAnnotation
    public interface Say {

        @CustomAnnotation
        String say();

        @CustomAnnotation
        default void hello() {}
    }

    @Documented
    @Retention(RUNTIME)
    public @interface CustomAnnotation {

    }
}
