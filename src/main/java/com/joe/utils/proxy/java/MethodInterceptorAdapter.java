package com.joe.utils.proxy.java;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.joe.utils.collection.CollectionUtil;
import com.joe.utils.common.Assert;
import com.joe.utils.proxy.Interception;
import com.joe.utils.proxy.ProxyParent;

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
    private final Object target;

    private final ProxyParent proxyParent;

    public MethodInterceptorAdapter(Object target, Class<?> targetClass, Interception interception) {
        Assert.notNull(targetClass);
        Assert.notNull(interception);
        this.target = target;
        this.interception = interception;
        this.proxyParent = new ProxyParent.InternalProxyParent(target, targetClass,
            CollectionUtil.addTo(ProxyParent.class, targetClass.getInterfaces()), interception);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 构建父方法调用
        if (ProxyParent.canInvoke(method)) {
            return Interception.invokeWrap(interception, null, method, proxy, args,
                () -> ProxyParent.invoke(method, proxyParent));
        } else {
            return Interception.invokeWrap(interception, target, method, proxy, args, null);
        }
    }
}
