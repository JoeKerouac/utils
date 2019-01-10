package com.joe.utils.proxy.cglib;

import com.joe.utils.proxy.Interception;
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
    public <T> T create(Class<T> parent, T proxy, ClassLoader loader, String name,
                        Interception interception) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(parent);
        enhancer.setClassLoader(loader);
        enhancer.setCallback(new MethodInterceptorAdapter(interception, proxy, parent));
        return (T) enhancer.create();
    }

    @Override
    public ClientType getClientType() {
        return ClientType.CGLIB;
    }
}
