package com.joe.utils.reflect;

import java.lang.reflect.Method;

/**
 * 调用分发接口，实现动态调用
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年02月19日 19:01 JoeKerouac Exp $
 */
public interface InvokeDistribute {

    /**
     * {@link InvokeDistribute#invoke(String, String, String, Object[])}方法名
     */
    String DISTRIBUTE_METHOD_NAME = "invoke";

    /**
     * {@link InvokeDistribute#invoke(String, String, String, Object[])}方法的byte code说明
     */
    String DISTRIBUTE_METHOD_DESC = ByteCodeUtils.getMethodDesc(ReflectUtil.getMethod(InvokeDistribute.class, "invoke",
        String.class, String.class, String.class, Object[].class));

    /**
     * 动态调用，根据给定参数调用不同的方法
     * 
     * @param owner
     *            方法拥有者的byte code表示，可以使用{@link ByteCodeUtils#convert(Class)}转换，注意，方法owner必须传最接近代
     *            理类的那个类，例如对A进行代理，A中声明了String say()方法，B中也声明了String say()方法，A继承B，此时要调用say方 法时owner应该传入A而不是B
     * @param methodName
     *            方法名
     * @param desc
     *            byte
     *            code方法说明，可以使用{@link ByteCodeUtils#getDesc(Class, Class[])}或者{@link ByteCodeUtils#getMethodDesc(Method)}获取
     * @param args
     *            调用参数
     * @return 调用结果，对于void方法结果为null
     * @throws NoSuchMethodException
     *             当要调用的方法owner传错、methodName传错、desc传错时将会抛出该异常
     */
    Object invoke(String owner, String methodName, String desc, Object... args) throws NoSuchMethodException;
}
