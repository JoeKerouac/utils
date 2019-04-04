package com.joe.utils.proxy.java;

import java.lang.reflect.Proxy;

import com.joe.utils.proxy.Interception;
import com.joe.utils.proxy.ProxyClient;
import com.joe.utils.proxy.ProxyException;
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
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> parent, T proxy, ClassLoader loader, String name,
                        Interception interception, Class<?>[] paramTypes, Object[] params) {
        return (T) Proxy.newProxyInstance(loader, new Class[] { parent, ProxyParent.class },
            new MethodInterceptorAdapter(proxy, parent, interception));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<T> createClass(Class<T> parent, T proxy, ClassLoader loader, String name,
                                    Interception interception) {
        // java代理返回class对象没有必要，而且构造器是一个特殊构造器，详情参照Proxy#newProxyInstance方法实现
        throw new ProxyException("不支持的操作");
    }
}
