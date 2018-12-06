package com.joe.utils.proxy;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.joe.utils.common.Assert;
import com.joe.utils.reflect.ClassUtils;
import com.joe.utils.scan.MethodScanner;

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
     * @param filter 方法拦截器
     * @param <T> 代理真实类型
     * @return 代理
     */
    default <T> T create(Class<T> parent, ProxyMethodFilter filter) {
        return createBuilder(parent).filterMethod(filter).build();
    }

    /**
     * 构建指定接口的代理Class，class必须是公共的，同时代理方法也必须是公共的
     * @param parent 指定接口
     * @param name 动态生成的Class的名字，例如com.joe.Test（注意：java原生代理对此不支持）
     * @param filter 方法拦截器
     * @param <T> 代理真实类型
     * @return 代理Class
     */
    default <T> T create(Class<T> parent, String name, ProxyMethodFilter filter) {
        return createBuilder(parent).name(name).filterMethod(filter).build();
    }

    /**
     * 构建指定接口的代理Class，接口必须是公共的，同时代理方法也必须是公共的
     * @param parent 指定接口
     * @param interceptionMap 方法映射
     * @param <T> 代理真实类型
     * @return 代理
     */
    default <T> T create(Class<T> parent, Map<MethodMetadata, Interception> interceptionMap) {
        Builder<T> builder = createBuilder(parent);
        interceptionMap.forEach(builder::proxyMethod);
        return builder.build();
    }

    /**
     * 创建一个Class构建器用于自定义构建Class，class必须是公共的，同时代理方法也必须是公共的
     * @param parent 要构建的Class的父类
     * @param <T> 父类类型
     * @return 代理构建器
     */
    <T> Builder<T> createBuilder(Class<T> parent);

    /**
     * 创建一个Class构建器用于自定义构建Class，class必须是公共的，同时代理方法也必须是公共的
     * @param parent 要构建的Class的父类
     * @param loader 要构建的Class的加载器
     * @param <T> 父类类型
     * @return 代理构建器
     */
    <T> Builder<T> createBuilder(Class<T> parent, ProxyClassLoader loader);

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

    /**
     * 代理构建器
     * @param <T> 构建的代理的父类类型
     */
    abstract class Builder<T> {

        /**
         * parent
         */
        protected final Class<T>                          parent;

        /**
         * 用于加载生成类的ClassLoader
         */
        protected final ProxyClassLoader                  loader;

        /**
         * 代理方法映射
         */
        protected final Map<MethodMetadata, Interception> proxyMap;

        /**
         * 创建的class的名字
         */
        protected String                                  name;

        protected Builder(Class<T> parent) {
            this(parent, ProxyClient.DEFAULT_LOADER);
        }

        protected Builder(Class<T> parent, ProxyClassLoader loader) {
            Assert.notNull(parent, "parent不能为null");
            Assert.notNull(loader, "loader不能为null");
            this.parent = parent;
            this.loader = loader;
            this.proxyMap = new HashMap<>();
        }

        /**
         * 拦截指定方法到另外的方法上
         * @param filter 方法拦截器
         * @return Builder
         */
        public Builder<T> filterMethod(ProxyMethodFilter filter) {
            List<Method> methods = MethodScanner.getInstance().scanByFilter(
                Collections.singletonList(method -> filter.filter(method) != null), parent);

            methods.forEach(
                method -> proxyMethod(MethodMetadata.build(method), filter.filter(method)));
            return this;
        }

        /**
         * 将指定metadata的方法代理到指定方法代理proxy上
         * @param metadata 被代理的方法的信息
         * @param proxy 代理方法
         * @return Builder
         */
        public Builder<T> proxyMethod(MethodMetadata metadata, Interception proxy) {
            if (metadata != null && proxy != null) {
                proxyMap.put(metadata, proxy);
            }
            return this;
        }

        /**
         * Builder构建的class的名字
         * @param name class名字，例如com.joe.Test
         * @return Builder
         */
        public Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        /**
         * 获取该builder的classloader
         * @return 该builder对应的classloader
         */
        public ProxyClassLoader getClassLoader() {
            return this.loader;
        }

        /**
         * 构建Class对象
         * @return 构建的代理对象，注意：多次调用该方法返回的代理对象不保证相同
         */
        public abstract T build();
    }
}
