package com.joe.utils.proxy;

import java.lang.reflect.Method;

import com.joe.utils.common.Assert;
import com.joe.utils.common.StringUtils;
import com.joe.utils.object.CustomObject;
import com.joe.utils.reflect.ReflectUtil;

/**
 * 所有代理都继承该类
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年01月11日 15:57 JoeKerouac Exp $
 */
public interface ProxyParent {

    /**
     * {@link #getTarget()}方法说明
     */
    MethodMetadata GET_TARGET       = new MethodMetadata("getTarget", ProxyParent.class,
        ReflectUtil.getMethod(ProxyParent.class, "getTarget"));

    /**
     * {@link #getTargetClass()}方法说明
     */
    MethodMetadata GET_TARGET_CLASS = new MethodMetadata("getTargetClass", ProxyParent.class,
        ReflectUtil.getMethod(ProxyParent.class, "getTargetClass"));

    /**
     * {@link #getInterfaces()}方法说明
     */
    MethodMetadata GET_INTERFACES   = new MethodMetadata("getInterfaces", ProxyParent.class,
        ReflectUtil.getMethod(ProxyParent.class, "getInterfaces"));

    /**
     * 获取代理的对象
     * @param <T> 代理对象类型
     * @return 代理的对象，如果是直接生成的代理类的实例而不是对指定对象生成代理则返回null
     */
    <T> T getTarget();

    /**
     * 实际代理的类型
     * @param <T> 实际代理的类型
     * @return 实际代理的类型的class对象
     */
    <T> Class<T> getTargetClass();

    /**
     * 获取实现的接口，不会返回代理类本身
     * @return 实现的接口集合
     */
    Class<?>[] getInterfaces();

    /**
     * 是否可以执行，即方法是否是{@link ProxyParent}声明的
     * @param method 方法
     * @return true表示可以执行，即可以调用{@link #invoke(Method, ProxyParent)}
     */
    static boolean canInvoke(Method method) {
        return CustomObject.of(MethodMetadata.build(method)).in(GET_TARGET, GET_TARGET_CLASS,
            GET_INTERFACES);
    }

    /**
     * 执行指定方法
     * @param method 方法，必须是{@link ProxyParent}声明的方法
     * @param proxyParent ProxyParent实例
     * @param <T> 返回值类型
     * @return 返回值
     */
    static <T> T invoke(Method method, ProxyParent proxyParent) {
        Assert.notNull(method);
        Assert.notNull(proxyParent);

        MethodMetadata metadata = MethodMetadata.build(method);
        if (canInvoke(method)) {
            return metadata.invoke(proxyParent);
        } else {
            throw new ProxyException(StringUtils.format("方法[{0}]不是ProxyParent中声明的", method));
        }
    }

    class InternalProxyParent implements ProxyParent {

        private final Object     target;

        private final Class<?>   targetClass;

        private final Class<?>[] interfaces;

        public InternalProxyParent(Object target, Class<?> targetClass, Class<?>[] interfaces) {
            this.target = target;
            this.targetClass = targetClass;
            this.interfaces = interfaces;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getTarget() {
            return (T) target;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Class<T> getTargetClass() {
            return (Class<T>) targetClass;
        }

        @Override
        public Class<?>[] getInterfaces() {
            return interfaces;
        }
    }
}