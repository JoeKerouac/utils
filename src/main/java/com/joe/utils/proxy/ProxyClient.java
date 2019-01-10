package com.joe.utils.proxy;

import com.joe.utils.reflect.ClassUtils;

/**
 * 代理客户端，代理的class必须是公共可访问的（如果是内部类那么必须是静态内部类）
 *
 * 注意java代理客户端{@link com.joe.utils.proxy.java.JavaProxyClient JavaProxyClient}的特殊性
 *
 * 不保证多次创建的代理对象{@link Object#getClass() getClass}方法的返回值相同
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月07日 11:17 JoeKerouac Exp $
 */
public interface ProxyClient {

    /**
     * ProxyClient的默认classloader
     */
    ProxyClassLoader DEFAULT_LOADER = new ProxyClassLoader(ProxyClient.class.getClassLoader());

    /**
     * 构建指定接口的代理Class，接口必须是公共的，同时代理方法也必须是公共的
     * @param parent 指定接口
     * @param proxy 方法代理
     * @param <T> 代理真实类型
     * @return 代理
     */
    default <T> T create(Class<T> parent, Interception proxy) {
        return create(parent, DEFAULT_LOADER, null, proxy);
    }

    /**
     * 构建指定接口的代理Class，接口必须是公共的，同时代理方法也必须是公共的
     * @param parent 指定接口
     * @param proxy 被代理的对象
     * @param interception 方法代理
     * @param <T> 代理真实类型
     * @return 代理
     */
    default <T> T create(Class<T> parent, T proxy, Interception interception) {
        return create(parent, proxy, DEFAULT_LOADER, null, interception);
    }

    /**
     * 构建指定接口的代理Class，接口必须是公共的，同时代理方法也必须是公共的
     * @param parent 指定接口
     * @param name 生成的对象的class名字，不一定支持（java代理不支持）
     * @param proxy 方法代理
     * @param <T> 代理真实类型
     * @return 代理
     */
    default <T> T create(Class<T> parent, String name, Interception proxy) {
        return create(parent, DEFAULT_LOADER, name, proxy);
    }

    /**
     * 构建指定接口的代理Class，接口必须是公共的，同时代理方法也必须是公共的
     * @param parent 指定接口
     * @param proxy 被代理的对象
     * @param name 生成的对象的class名字，不一定支持（java代理不支持）
     * @param interception 方法代理
     * @param <T> 代理真实类型
     * @return 代理
     */
    default <T> T create(Class<T> parent, T proxy, String name, Interception interception) {
        return create(parent, proxy, DEFAULT_LOADER, name, interception);
    }

    /**
     * 构建指定接口的代理Class，接口必须是公共的，同时代理方法也必须是公共的
     * @param parent 指定接口
     * @param loader 加载生成的对象的class的classloader
     * @param proxy 方法代理
     * @param <T> 代理真实类型
     * @return 代理
     */
    default <T> T create(Class<T> parent, ClassLoader loader, Interception proxy) {
        return create(parent, loader, null, proxy);
    }

    /**
     * 构建指定接口的代理Class，接口必须是公共的，同时代理方法也必须是公共的
     * @param parent 指定接口
     * @param proxy 被代理的对象
     * @param loader 加载生成的对象的class的classloader
     * @param interception 方法代理
     * @param <T> 代理真实类型
     * @return 代理
     */
    default <T> T create(Class<T> parent, T proxy, ClassLoader loader, Interception interception) {
        return create(parent, proxy, loader, null, interception);
    }

    /**
     * 构建指定接口的代理Class，接口必须是公共的，同时代理方法也必须是公共的
     * @param parent 指定接口
     * @param loader 加载生成的对象的class的classloader
     * @param name 生成的对象的class名字，不一定支持（java代理不支持）
     * @param proxy 方法代理
     * @param <T> 代理真实类型
     * @return 代理
     */
    default <T> T create(Class<T> parent, ClassLoader loader, String name, Interception proxy) {
        return create(parent, null, loader, name, proxy);
    }

    /**
     * 构建指定对象的代理，对象的类必须是公共的，同时代理方法也必须是公共的
     * @param parent 指定接口
     * @param proxy 被代理的对象
     * @param loader 加载生成的对象的class的classloader
     * @param name 生成的对象的class名字，不一定支持（java代理不支持）
     * @param interception 方法代理
     * @param <T> 代理真实类型
     * @return 代理
     */
    <T> T create(Class<T> parent, T proxy, ClassLoader loader, String name,
                 Interception interception);

    /**
     * 获取代理客户端的类型
     * @return 代理客户端类型
     */
    ClientType getClientType();

    /**
     * 获取指定类型的代理客户端
     * @param type 代理客户端类型
     * @return 客户端
     */
    static ProxyClient getInstance(ClientType type) {
        return ClassUtils.getInstance(type.clientClass);
    }

    /**
     * 
     */
    enum ClientType {
                     /**
                      * CGLIB代理客户端
                      */
                     CGLIB("com.joe.utils.proxy.cglib.CglibProxyClient"),

                     /**
                      * ByteBuddy客户端
                      */
                     BYTE_BUDDY("com.joe.utils.proxy.bytebuddy.ByteBuddyProxyClient"),

                     /**
                      * JAVA代理客户端
                      */
                     JAVA("com.joe.utils.proxy.java.JavaProxyClient");

        private String clientClass;

        ClientType(String clientClass) {
            this.clientClass = clientClass;
        }
    }
}
