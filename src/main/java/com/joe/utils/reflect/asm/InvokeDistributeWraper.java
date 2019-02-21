package com.joe.utils.reflect.asm;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.joe.utils.common.Assert;
import com.joe.utils.common.StringUtils;
import com.joe.utils.exception.InvokeException;
import com.joe.utils.reflect.*;

/**
 * InvokeDistribute包装对象，用于包装{@link com.joe.utils.reflect.InvokeDistributeFactory InvokeDistributeFactory}生成的对象，方便调用方法
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年02月21日 18:00 JoeKerouac Exp $
 */
public class InvokeDistributeWraper<T> {
    private static final InvokeDistributeFactory FACTORY = new AsmInvokeDistributeFactory();

    /**
     * {@link com.joe.utils.reflect.InvokeDistributeFactory InvokeDistributeFactory}生成的代理对象
     */
    private InvokeDistribute                     proxy;

    /**
     * 实际对象
     */
    private T                                    realTarget;

    public InvokeDistributeWraper(T target) {
        this(target, null, null);
    }

    public InvokeDistributeWraper(T target, String className) {
        this(target, className, null);
    }

    public InvokeDistributeWraper(T target, String className, DynamicClassLoader classLoader) {
        Assert.notNull(target);

        this.proxy = FACTORY.build(target, className, classLoader);
        this.realTarget = target;
    }

    /**
     * 根据方法名调用方法
     * @param methodName 方法名
     * @param paramTypes 根据方法
     * @param params 调用方法的参数
     * @param <R> 方法返回值类型
     * @return 返回值
     */
    public <R> R invoke(String methodName, Class<?>[] paramTypes, Object[] params) {
        Method method = ReflectUtil.getMethod(realTarget.getClass(), methodName, paramTypes);
        return invoke(method, params);
    }

    /**
     * 根据方法对象调用方法，只能调用非static的public或者protected方法
     * @param method 要调用的方法
     * @param params 调用方法的参数
     * @param <R> 方法返回值类型
     * @return 返回值
     */
    @SuppressWarnings("unchecked")
    public <R> R invoke(Method method, Object[] params) {
        Assert.notNull(method);
        if (!method.getDeclaringClass().isAssignableFrom(realTarget.getClass())) {
            throw new InvokeException(StringUtils.format("方法[{0}]不是指定类[{1}]或者其父类的", method.getName(),
                realTarget.getClass()));
        }

        int modifier = method.getModifiers();
        if ((!Modifier.isProtected(modifier) && !Modifier.isPublic(modifier))
            || Modifier.isStatic(modifier)) {
            throw new InvokeException(
                StringUtils.format("只能调用非static的public或者protected方法，要调用的方法为：[{}]", method));
        }

        return (R) proxy.invoke(ByteCodeUtils.convert(method.getDeclaringClass()), method.getName(),
            ByteCodeUtils.getMethodDesc(method), params);
    }

    /**
     * 获取代理对象
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    public T getProxy() {
        return (T) proxy;
    }

    /**
     * 获取实际对象（通过构造器传入的对象）
     * @return 实际对象
     */
    public T getRealTarget() {
        return realTarget;
    }
}
