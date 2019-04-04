package com.joe.utils.proxy.cglib;

import com.joe.utils.collection.CollectionUtil;
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
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> parent, T proxy, ClassLoader loader, String name,
                        Interception interception, Class<?>[] paramTypes, Object[] params) {
        if (!CollectionUtil.sizeEquals(params, paramTypes)) {
            throw new IllegalArgumentException("构造器参数列表paramTypes长度和实际参数params长度不一致");
        }

        Enhancer enhancer = new Enhancer();
        if (parent.isInterface()) {
            enhancer.setInterfaces(new Class[] { ProxyParent.class, parent });
        } else {
            enhancer.setSuperclass(parent);
            enhancer.setInterfaces(new Class[] { ProxyParent.class });
        }
        enhancer.setClassLoader(loader);
        enhancer.setCallback(new MethodInterceptorAdapter(interception, proxy, parent));
        if (CollectionUtil.safeIsEmpty(paramTypes)) {
            return (T) enhancer.create();
        }else{
            return (T) enhancer.create(paramTypes, params);
        }
    }

    /**
     * 构建指定对象的代理Class，稍后可以通过反射构建该class的实例，对象的类必须是公共的，同时代理方法也必须是公共的
     * <p>
     *     注意：生成的class通过反射调用构造器创建对象的时候，构造器中调用的方法不会被拦截！！！
     * </p>
     *
     * @param parent 指定接口
     * @param proxy 被代理的对象
     * @param loader 加载生成的对象的class的classloader
     * @param name 生成的对象的class名字，不一定支持（java代理不支持）
     * @param interception 方法代理
     * @param <T> 代理真实类型
     * @return 代理class
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> createClass(Class<T> parent, T proxy, ClassLoader loader,
                                              String name, Interception interception) {
        Enhancer enhancer = new Enhancer();
        if (parent.isInterface()) {
            enhancer.setInterfaces(new Class[] { ProxyParent.class, parent });
        } else {
            enhancer.setSuperclass(parent);
            enhancer.setInterfaces(new Class[] { ProxyParent.class });
        }
        enhancer.setClassLoader(loader);
        enhancer.setCallback(new MethodInterceptorAdapter(interception, proxy, parent));
        return (Class<? extends T>) enhancer.createClass();
    }

    @Override
    public ClientType getClientType() {
        return ClientType.CGLIB;
    }
}
