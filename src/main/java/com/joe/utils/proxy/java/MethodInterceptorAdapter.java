package com.joe.utils.proxy.java;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.joe.utils.common.Assert;
import com.joe.utils.proxy.Interception;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年12月05日 11:25 JoeKerouac Exp $
 */
public class MethodInterceptorAdapter implements InvocationHandler {

    /**
     * 代理方法
     */
    private final Interception interception;

    /**
     * 代理的方法
     */
    private final Object       target;

    public MethodInterceptorAdapter(Object target, Class<?> targetClass,
                                    Interception interception) {
        Assert.notNull(targetClass);
        Assert.notNull(interception);
        this.target = target;
        this.interception = interception;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 构建父方法调用
        return Interception.invokeWrap(interception, target, method, proxy, args, null);
    }
}
