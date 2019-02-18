package com.joe.utils.proxy.cglib;

import java.lang.reflect.Method;

import com.joe.utils.collection.CollectionUtil;
import com.joe.utils.common.Assert;
import com.joe.utils.proxy.Interception;
import com.joe.utils.proxy.Invoker;
import com.joe.utils.proxy.ProxyParent;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * cglib方法拦截适配器
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月10日 23:17 JoeKerouac Exp $
 */
public class MethodInterceptorAdapter implements MethodInterceptor {

    /**
     * 代理方法实现
     */
    private final Interception proxy;

    /**
     * target，可以为空，为空表示生成新代理，不为空表示对target代理
     */
    private final Object       target;

    private final Class<?>     parent;

    private final ProxyParent  proxyParent;

    public MethodInterceptorAdapter(Interception proxy, Object target, Class<?> parent) {
        Assert.notNull(proxy);
        Assert.notNull(parent);
        this.proxy = proxy;
        this.target = target;
        this.parent = parent;

        this.proxyParent = new ProxyParent.InternalProxyParent(target, parent,
            CollectionUtil.addTo(ProxyParent.class, parent.getInterfaces(), Class<?>[]::new));
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args,
                            MethodProxy methodProxy) throws Throwable {
        Invoker supperCall = null;
        if (!parent.isInterface()) {
            supperCall = () -> methodProxy.invokeSuper(obj, args);
        }

        if (ProxyParent.canInvoke(method)) {
            return Interception.invokeWrap(proxy, null, method, obj, args,
                () -> ProxyParent.invoke(method, proxyParent));
        } else {
            return Interception.invokeWrap(proxy, target, method, obj, args, supperCall);
        }
    }
}
