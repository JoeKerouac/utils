package com.joe.utils.proxy.java;

import java.lang.reflect.Proxy;

import com.joe.utils.proxy.Interception;
import com.joe.utils.proxy.ProxyClient;
import com.joe.utils.proxy.ProxyParent;

/**
 * 需要注意的是java原生代理客户端只支持对接口的代理，不支持对普通类或者抽象类代理，同时不支持设置代理生成的类的名字
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年12月05日 11:20 JoeKerouac Exp $
 */
public class JavaProxyClient implements ProxyClient {

    @Override
    public ClientType getClientType() {
        return ClientType.JAVA;
    }

    @Override
    public <T> T create(Class<T> parent, T proxy, ClassLoader loader, String name,
                        Interception interception) {
        return (T) Proxy.newProxyInstance(loader, new Class[] { parent, ProxyParent.class },
            new MethodInterceptorAdapter(proxy, parent, interception));
    }
}
