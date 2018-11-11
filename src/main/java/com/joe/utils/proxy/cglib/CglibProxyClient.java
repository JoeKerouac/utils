package com.joe.utils.proxy.cglib;

import com.joe.utils.proxy.ProxyClassLoader;
import com.joe.utils.proxy.ProxyClient;

import net.sf.cglib.proxy.Enhancer;

/**
 * cglib实现的代理客户端
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月10日 23:15 JoeKerouac Exp $
 */
public class CglibProxyClient implements ProxyClient {

    @Override
    public <T> Builder<T> createBuilder(Class<T> parent) {
        return new DefaultBuilder<>(parent);
    }

    @Override
    public <T> Builder<T> createBuilder(Class<T> parent, ProxyClassLoader loader) {
        return new DefaultBuilder<>(parent, loader);
    }

    private static class DefaultBuilder<T> extends Builder<T>{

        DefaultBuilder(Class<T> parent) {
            super(parent);
        }

        DefaultBuilder(Class<T> parent, ProxyClassLoader loader) {
            super(parent, loader);
        }

        @Override
        @SuppressWarnings("unchecked")
        public T build() {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(parent);
            enhancer.setClassLoader(loader);
            enhancer.setCallback(new MethodInterceptorAdapter(proxyMap));
            return (T)enhancer.create();
        }
    }
}
