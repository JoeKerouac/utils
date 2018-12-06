package com.joe.utils.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

import com.joe.utils.common.Assert;

import lombok.Data;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月07日 11:21 JoeKerouac Exp $
 */
@Data
public class MethodMetadata {
    private static final Type[]              NULL = new Type[0];

    /**
     * Object中声明的方法集合
     */
    public static final List<MethodMetadata> OBJECT_METHOD;

    /**
     * {@link Object#getClass() getClass}方法
     */
    public static final MethodMetadata       GET_CLASS_META;

    /**
     * {@link Object#hashCode() hashCode}方法
     */
    public static final MethodMetadata       HASH_CODE_META;

    /**
     * {@link Object#equals(Object) equals}方法
     */
    public static final MethodMetadata       EQUALS_META;

    /**
     * {@link Object#clone() clone}方法
     */
    public static final MethodMetadata       CLONE_META;

    /**
     * {@link Object#toString() toString}方法
     */
    public static final MethodMetadata       TO_STRING_META;

    /**
     * {@link Object#notify() notify方法}
     */
    public static final MethodMetadata       NOTIFY_META;

    /**
     * {@link Object#notifyAll() notifyAll}方法
     */
    public static final MethodMetadata       NOTIFY_ALL_META;

    /**
     * {@link Object#wait() wait}方法
     */
    public static final MethodMetadata       WAIT_META;

    /**
     * {@link Object#wait(long) wait}方法
     */
    public static final MethodMetadata       WAIT1_META;

    /**
     * {@link Object#wait(long, int) wait}方法
     */
    public static final MethodMetadata       WAIT2_META;

    /**
     * {@link Object#finalize() finalize方法}
     */
    public static final MethodMetadata       FINALIZE_META;

    /**
     * 方法名
     */
    private final String                     name;

    /**
     * 方法返回值类型
     */
    private final Type                       returnType;

    /**
     * 方法参数类型列表
     */
    private final Type[]                     params;

    /**
     * 方法访问权限
     */
    private final int                        modifier;

    public MethodMetadata(String name, Type returnType, Type[] params) {
        this(name, returnType, params, Modifier.PUBLIC);
    }

    public MethodMetadata(String name, Type returnType, Type[] params, int modifier) {
        Assert.notNull(name, "name不能为null");
        Assert.notNull(returnType, "returnType不能为null");
        this.name = name;
        this.returnType = returnType;
        this.params = params == null ? NULL : params;
        this.modifier = modifier;
    }

    static {
        OBJECT_METHOD = new ArrayList<>(11);
        GET_CLASS_META = new MethodMetadata("getClass", Class.class, null);
        HASH_CODE_META = new MethodMetadata("hashCode", int.class, null);
        EQUALS_META = new MethodMetadata("equals", boolean.class, new Type[] { Object.class });
        CLONE_META = new MethodMetadata("clone", Object.class, null);
        TO_STRING_META = new MethodMetadata("toString", String.class, null);
        NOTIFY_META = new MethodMetadata("notify", void.class, null);
        NOTIFY_ALL_META = new MethodMetadata("notifyAll", void.class, null);
        WAIT_META = new MethodMetadata("wait", void.class, null);
        WAIT1_META = new MethodMetadata("wait", void.class, new Type[] { long.class });
        WAIT2_META = new MethodMetadata("wait", void.class, new Type[] { long.class, int.class });
        FINALIZE_META = new MethodMetadata("finalize", void.class, null);
        OBJECT_METHOD.add(GET_CLASS_META);
        OBJECT_METHOD.add(HASH_CODE_META);
        OBJECT_METHOD.add(EQUALS_META);
        OBJECT_METHOD.add(CLONE_META);
        OBJECT_METHOD.add(TO_STRING_META);
        OBJECT_METHOD.add(NOTIFY_META);
        OBJECT_METHOD.add(NOTIFY_ALL_META);
        OBJECT_METHOD.add(WAIT_META);
        OBJECT_METHOD.add(WAIT1_META);
        OBJECT_METHOD.add(WAIT2_META);
        OBJECT_METHOD.add(FINALIZE_META);
    }

    /**
     * 根据method构建method元数据
     * @param method method
     * @return method元数据
     */
    public static MethodMetadata build(Method method) {
        return new MethodMetadata(method.getName(), method.getReturnType(),
            method.getParameterTypes(), method.getModifiers());
    }

    /**
     * 从代理方法集合中筛选出来该方法对应的代理
     * @param method 要代理的方法
     * @param proxyMap 要代理的方法集合
     * @return 代理方法，不对要代理的方法进行代理返回null
     */
    public static Interception filter(Method method, Map<MethodMetadata, Interception> proxyMap) {
        for (Map.Entry<MethodMetadata, Interception> entry : proxyMap.entrySet()) {
            MethodMetadata metadata = entry.getKey();
            if (metadata != null && metadata.equals(method)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 判断方法是不是Object的方法（例如equals、toStirng等）
     * @param method 方法
     * @return 返回true表示是Object类的方法
     */
    public static boolean isObjectMethod(Method method) {
        return OBJECT_METHOD.stream().filter(meta -> meta.equals(method)).limit(1).count() > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof MethodMetadata) {
            MethodMetadata that = (MethodMetadata) o;
            return Objects.equals(name, that.name) && Arrays.equals(params, that.params);
        } else if (o instanceof Method) {
            Method method = (Method) o;
            return Objects.equals(name, method.getName())
                   && Arrays.equals(params, method.getParameterTypes());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(params);
        return result;
    }
}
