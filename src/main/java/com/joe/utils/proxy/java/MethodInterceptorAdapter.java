package com.joe.utils.proxy.java;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import com.joe.utils.common.StringUtils;
import com.joe.utils.proxy.Interception;
import com.joe.utils.proxy.Invoker;
import com.joe.utils.proxy.MethodMetadata;
import com.joe.utils.proxy.ProxyException;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年12月05日 11:25 JoeKerouac Exp $
 */
public class MethodInterceptorAdapter implements InvocationHandler {

    /**
     * 代理方法集合
     */
    private final Map<MethodMetadata, Interception> proxyMap;

    public MethodInterceptorAdapter(Map<MethodMetadata, Interception> proxyMap) {
        this.proxyMap = proxyMap;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Interception interception = MethodMetadata.filter(method, proxyMap);
        // 构建父方法调用
        Invoker invoker = buildObjectMethod(proxy, method, args);

        // 判断用户是否指定方法的代理，如果用户没有指明被代理方法的代理方法，那么尝试调用父类方法，如果父类方法也无法调用（是接口方法），
        // 那么将抛出异常
        if (interception == null) {
            if (invoker == null) {
                throw new ProxyException(
                    StringUtils.format("Java代理必须对接口的所有方法进行代理，当前方法[{}]未被代理", method));
            } else {
                return invoker.call();
            }
        } else {
            return interception.invoke(args, invoker, method);
        }
    }

    /**
     * 构建指定方法的父类调用（如果不能进行父类调用那么返回null）
     * @param proxy 被代理的对象（对于java代理来说就是代理本身）
     * @param method 被代理的方法
     * @param args 被代理的方法的调用参数
     * @return 父类方法调用
     */
    private Invoker buildObjectMethod(Object proxy, Method method, Object[] args) {
        Object proxyObj = new Object();
        if (MethodMetadata.HASH_CODE_META.equals(method)) {
            return proxyObj::hashCode;
        } else if (MethodMetadata.TO_STRING_META.equals(method)) {
            String toString = proxyObj.toString().replace("java.lang.Object",
                proxy.getClass().getName() + "$$JavaClientProxy");
            return () -> toString;
        } else if (MethodMetadata.FINALIZE_META.equals(method)) {
            return () -> {
                throw new ProxyException("Java代理不支持finalize方法");
            };
        } else if (MethodMetadata.EQUALS_META.equals(method)) {
            return () -> proxy == args[0];
        } else if (MethodMetadata.isObjectMethod(method)) {
            return () -> method.invoke(proxy, args);
        }
        return null;
    }
}
