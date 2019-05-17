package com.joe.utils.proxy;

import java.lang.reflect.Method;

import com.joe.utils.common.Assert;

/**
 * 拦截点，拦截到方法后执行拦截点
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月08日 20:43 JoeKerouac Exp $
 */
public interface Interception {

    /**
     * 拦截点执行，可以使用invoker对父类方法发起调用
     * @param target 代理的对象（如果是静态方法那么该target为null）
     * @param params 方法调用参数
     * @param method 拦截的方法
     * @param invoker 父类方法调用（可能为null，为null时表示无法调用父类方法）
     * @return 拦截点执行结果
     * @throws Throwable 执行异常
     */
    Object invoke(Object target, Object[] params, Method method, Invoker invoker) throws Throwable;

    /**
     * 拦截方法包装执行
     * @param interception 拦截的方法的代理，不能为null
     * @param target 被代理的对象，只有对指定对象代理时才会有值，其他情况为null
     * @param method 被代理的方法，不能为null
     * @param realTarget 代理target，可以为null
     * @param params 执行方法的参数，可以为null
     * @param superCall 父类调用，可以为null
     * @return 方法执行结果
     * @throws Throwable Throwable
     */
    static Object invokeWrap(Interception interception, Object target, Method method,
                             Object realTarget, Object[] params,
                             Invoker superCall) throws Throwable {
        Assert.notNull(interception);
        Assert.notNull(method);

        Invoker invoker;

        // 如果target是null那么说明是对类生成代理，否则说明是要对指定对象进行代理
        if (target == null) {
            invoker = superCall;
        } else {
            invoker = buildInvoker(target, method, params);
        }

        Object invokeObj = target == null ? realTarget : target;

        // 如果还是null那说明是直接生成指定类的代理而不是指定对象的代理，并且该方法是Object的方法
        if (invoker == null) {
            invoker = buildObjectMethod(invokeObj, method, params);
        }

        return interception.invoke(invokeObj, params, method, invoker);
    }

    /**
     * 构建指定对象指定方法的父调用，如果指定对象是代理对象，那么将会调用该对象的代理方法，否则将会直接使用指定对象反射调用指定方法
     * @param obj 指定对象
     * @param method 指定方法
     * @param params 参数
     * @return 指定方法的父调用
     */
    static Invoker buildInvoker(Object obj, Method method, Object[] params) {
        if (obj instanceof ProxyParent) {
            ProxyParent proxyParent = (ProxyParent) obj;
            Object target = proxyParent.GET_TARGET();
            Interception parentInterception = proxyParent.GET_INTERCEPTION();
            return () -> parentInterception.invoke(target, params, method,
                buildInvoker(target, method, params));
        }
        return () -> method.invoke(obj, params);
    }

    /**
     * 构建指定方法的父类调用（如果不能进行父类调用那么返回null）
     * @param proxy 被代理的对象（对于java代理来说就是代理本身）
     * @param method 被代理的方法
     * @param args 被代理的方法的调用参数
     * @return 父类方法调用
     */
    static Invoker buildObjectMethod(Object proxy, Method method, Object[] args) {
        Object proxyObj = new Object();
        if (MethodMetadata.HASH_CODE_META.equals(method)) {
            return proxyObj::hashCode;
        } else if (MethodMetadata.TO_STRING_META.equals(method)) {
            String toString = proxyObj.toString().replace("java.lang.Object",
                proxy.getClass().getName() + "$$Proxy");
            return () -> toString;
        } else if (MethodMetadata.EQUALS_META.equals(method)) {
            return () -> proxy == args[0];
        } else if (MethodMetadata.isObjectMethod(method)) {
            return () -> method.invoke(proxy, args);
        } else {
            return null;
        }
    }
}
