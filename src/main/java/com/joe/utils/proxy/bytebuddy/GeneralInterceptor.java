package com.joe.utils.proxy.bytebuddy;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import com.joe.utils.collection.CollectionUtil;
import com.joe.utils.common.Assert;
import com.joe.utils.proxy.Interception;
import com.joe.utils.proxy.ProxyParent;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

/**
 * 方法拦截器
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月09日 11:41 JoeKerouac Exp $
 */
public class GeneralInterceptor {

    /**
     * 代理方法实现
     */
    private final Interception interception;

    /**
     * target，可以为空，为空表示生成新代理，不为空表示对target代理
     */
    private final Object       target;

    private final ProxyParent  proxyParent;

    public GeneralInterceptor(Interception interception, Class<?> parent) {
        this(interception, parent, null);
    }

    public GeneralInterceptor(Interception interception, Class<?> parent, Object target) {
        Assert.notNull(interception);
        Assert.notNull(parent);
        this.interception = interception;
        this.target = target;
        this.proxyParent = new ProxyParent.InternalProxyParent(target, parent,
            CollectionUtil.addTo(ProxyParent.class, parent.getInterfaces(), Class<?>[]::new));
    }

    /**
     * 拦截有实现的方法
     * @param params 调用方法的参数
     * @param method 被拦截的方法
     * @param callable 父类调用
     * @return 执行结果
     */
    @RuntimeType
    public Object interceptClass(@AllArguments Object[] params, @Origin Method method,
                                 @SuperCall Callable<Object> callable) throws Throwable {
        return Interception.invokeWrap(interception, this.target, method, null, params,
            callable::call);
    }

    /**
     * 拦截抽象方法
     * @param params 调用方法的参数
     * @param method 被拦截的方法
     * @return 执行结果
     */
    @RuntimeType
    public Object interceptInterface(@AllArguments Object[] params,
                                     @Origin Method method) throws Throwable {
        if (ProxyParent.canInvoke(method)) {
            return Interception.invokeWrap(interception, null, method, null, params,
                () -> ProxyParent.invoke(method, proxyParent));
        } else {
            return Interception.invokeWrap(interception, null, method, null, params, null);
        }
    }
}
