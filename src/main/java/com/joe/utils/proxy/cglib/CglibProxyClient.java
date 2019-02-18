package com.joe.utils.proxy.cglib;

import com.joe.utils.proxy.Interception;
import com.joe.utils.proxy.ProxyClient;
import com.joe.utils.proxy.ProxyParent;

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
        if (parent.isInterface()) {
            enhancer.setInterfaces(new Class[] { ProxyParent.class, parent });
        } else {
            enhancer.setSuperclass(parent);
            enhancer.setInterfaces(new Class[] { ProxyParent.class });
        }
        enhancer.setClassLoader(loader);
        enhancer.setCallback(new MethodInterceptorAdapter(interception, proxy, parent));
        return (T) enhancer.create();
    }

    @Override
    public ClientType getClientType() {
        return ClientType.CGLIB;
    }
}
