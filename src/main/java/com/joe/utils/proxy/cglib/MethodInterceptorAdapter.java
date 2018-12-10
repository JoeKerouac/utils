package com.joe.utils.proxy.cglib;

import java.lang.reflect.Method;
import java.util.Map;

import com.joe.utils.proxy.Interception;
import com.joe.utils.proxy.MethodMetadata;
import com.joe.utils.proxy.ProxyException;

import com.joe.utils.reflect.ReflectUtil;
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
     * 代理方法集合
     */
    private final Map<MethodMetadata, Interception> proxyMap;

    public MethodInterceptorAdapter(Map<MethodMetadata, Interception> proxyMap) {
        this.proxyMap = proxyMap;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) {
        Interception interception = MethodMetadata.filter(method, proxyMap);
        try {
            if (interception == null) {
                return proxy.invokeSuper(obj, args);
            } else {
                // cglib不支持调用接口中声明的default方法
                if (ReflectUtil.isAbstract(method) || method.getDeclaringClass().isInterface()) {
                    return interception.invoke(args, null, method);
                } else {
                    return interception.invoke(args, () -> proxy.invokeSuper(obj, args), method);
                }
            }
        } catch (Throwable e) {
            throw new ProxyException(e);
        }
    }
}
