package com.joe.utils.reflect;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年04月03日 17:18 JoeKerouac Exp $
 */
public class AccessorUtil {

    /**
     * 判断字段是否是final的
     *
     * @param field
     *            字段
     * @return 返回true表示是final
     */
    public static boolean isFinal(Field field) {
        int modifier = field.getModifiers();
        return isFinal(modifier);
    }

    /**
     * 判断方法、构造器是否是final
     *
     * @param executable
     *            方法、构造器对象
     * @return 返回true表示是final
     */
    public static boolean isFinal(Executable executable) {
        int modifier = executable.getModifiers();
        return isFinal(modifier);
    }

    /**
     * 判断字段是否是public
     *
     * @param field
     *            字段
     * @return 返回true表示是public
     */
    public static boolean isPublic(Field field) {
        int modifier = field.getModifiers();
        return isPublic(modifier);
    }

    /**
     * 判断方法、构造器是否是public
     *
     * @param executable
     *            方法、构造器对象
     * @return 返回true表示是public
     */
    public static boolean isPublic(Executable executable) {
        int modifier = executable.getModifiers();
        return isPublic(modifier);
    }

    /**
     * 判断方法、构造器是否是protected
     *
     * @param executable
     *            方法、构造器对象
     * @return 返回true表示是protected
     */
    public static boolean isProtected(Executable executable) {
        int modifier = executable.getModifiers();
        return isProtected(modifier);
    }

    /**
     * 判断方法是否是static
     * 
     * @param method
     *            方法
     * @return true表示方法是静态的
     */
    public static boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    /**
     * 判断方法是否是抽象的
     * 
     * @param method
     *            方法
     * @return 返回true表示方法是抽象的
     */
    public static boolean isAbstract(Method method) {
        return Modifier.isAbstract(method.getModifiers());
    }

    /**
     * 判断字段是否是static
     * 
     * @param field
     *            字段
     * @return true表示字段是静态的
     */
    public static boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    /**
     * 判断修饰符是否是final
     *
     * @param modifier
     *            修饰符
     * @return 返回true表示是final类型
     */
    private static boolean isFinal(int modifier) {
        return Modifier.isFinal(modifier);
    }

    /**
     * 判断修饰符是否是public
     *
     * @param modifier
     *            修饰符
     * @return 返回true表示是public
     */
    private static boolean isPublic(int modifier) {
        return Modifier.isPublic(modifier);
    }

    /**
     * 判断修饰符是否是protected
     *
     * @param modifier
     *            修饰符
     * @return 返回true表示是protected
     */
    private static boolean isProtected(int modifier) {
        return Modifier.isProtected(modifier);
    }
}
