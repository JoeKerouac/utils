package com.joe.utils.proxy.java;

import java.lang.reflect.Proxy;

import com.joe.utils.proxy.ProxyClassLoader;
import com.joe.utils.proxy.ProxyClient;

/**
 * 需要注意的是java原生代理客户端只支持对接口的代理，不支持对普通类或者抽象类代理，同时不支持设置代理生成的类的名字
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年12月05日 11:20 JoeKerouac Exp $
 */
public class JavaProxyClient implements ProxyClient {
    @Override
    public <T> Builder<T> createBuilder(Class<T> parent) {
        return new DefaultBuilder<>(parent);
    }

    @Override
    public <T> Builder<T> createBuilder(Class<T> parent, ProxyClassLoader loader) {
        return new DefaultBuilder<>(parent, loader);
    }

    @Override
    public ClientType getClientType() {
        return ClientType.JAVA;
    }

    private static class DefaultBuilder<T> extends Builder<T> {

        DefaultBuilder(Class<T> parent) {
            super(parent);
        }

        DefaultBuilder(Class<T> parent, ProxyClassLoader loader) {
            super(parent, loader);
        }

        @Override
        @SuppressWarnings("unchecked")
        public T build() {
            if (!parent.isInterface()) {
                throw new IllegalArgumentException("java代理客户端只能对接口生成代理");
            }
            return (T) Proxy.newProxyInstance(loader, new Class[] { parent },
                new MethodInterceptorAdapter(proxyMap));
        }
    }
}
