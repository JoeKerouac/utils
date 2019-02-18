package com.joe.utils.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.joe.utils.common.Assert;
import com.joe.utils.reflect.ReflectUtil;

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
     * 声明类
     */
    private final Class<?>                   declareClass;

    /**
     * 方法参数类型列表
     */
    private final Type[]                     params;

    /**
     * 对应的方法，可以为null
     */
    private Method                           method;

    public MethodMetadata(String name, Class<?> declareClass, Method method, Type... params) {
        Assert.notNull(name, "name不能为null");
        Assert.notNull(method, "method不能为null");
        Assert.notNull(declareClass, "declareClass不能为null");
        this.name = name;
        this.declareClass = declareClass;
        this.method = method;
        this.params = params == null ? NULL : params;
    }

    static {
        try {
            OBJECT_METHOD = new ArrayList<>(11);
            GET_CLASS_META = new MethodMetadata("getClass", Object.class,
                Object.class.getDeclaredMethod("getClass"));
            HASH_CODE_META = new MethodMetadata("hashCode", Object.class,
                Object.class.getDeclaredMethod("hashCode"));
            EQUALS_META = new MethodMetadata("equals", Object.class,
                Object.class.getDeclaredMethod("equals", Object.class), Object.class);
            CLONE_META = new MethodMetadata("clone", Object.class,
                Object.class.getDeclaredMethod("clone"));
            TO_STRING_META = new MethodMetadata("toString", Object.class,
                Object.class.getDeclaredMethod("toString"));
            NOTIFY_META = new MethodMetadata("notify", Object.class,
                Object.class.getDeclaredMethod("notify"));
            NOTIFY_ALL_META = new MethodMetadata("notifyAll", Object.class,
                Object.class.getDeclaredMethod("notifyAll"));
            WAIT_META = new MethodMetadata("wait", Object.class,
                Object.class.getDeclaredMethod("wait"));
            WAIT1_META = new MethodMetadata("wait", Object.class,
                Object.class.getDeclaredMethod("wait", long.class), long.class);
            WAIT2_META = new MethodMetadata("wait", Object.class,
                Object.class.getDeclaredMethod("wait", long.class, int.class), long.class,
                int.class);
            FINALIZE_META = new MethodMetadata("finalize", Object.class,
                Object.class.getDeclaredMethod("finalize"));
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
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据method构建method元数据
     * @param method method
     * @return method元数据
     */
    public static MethodMetadata build(Method method) {
        return new MethodMetadata(method.getName(), method.getDeclaringClass(), method,
            method.getParameterTypes());
    }

    /**
     * 判断方法是不是Object的方法（例如equals、toStirng等）
     * @param method 方法
     * @return 返回true表示是Object类的方法
     */
    public static boolean isObjectMethod(Method method) {
        return OBJECT_METHOD.stream().filter(meta -> meta.equals(method)).limit(1).count() > 0;
    }

    /**
     * 使用指定对象和参数执行该方法，相当于调用{@link Method#invoke(Object, Object...)}
     * @param target 对象
     * @param args 参数
     * @return 执行结果
     */
    public <T> T invoke(Object target, Object... args) {
        return ReflectUtil.invoke(target, method, args);
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
