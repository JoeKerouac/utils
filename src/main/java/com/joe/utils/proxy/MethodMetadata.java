package com.joe.utils.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

import com.joe.utils.common.Assert;

import lombok.Data;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月07日 11:21 JoeKerouac Exp $
 */
@Data
public class MethodMetadata {
    private static final Type[] NULL = new Type[0];

    /**
     * 方法名
     */
    private final String        name;

    /**
     * 方法返回值类型
     */
    private final Type          returnType;

    /**
     * 方法参数类型列表
     */
    private final Type[]        params;

    /**
     * 方法访问权限
     */
    private final int           modifier;

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

    /**
     * 根据method构建method元数据
     * @param method method
     * @return method元数据
     */
    public static MethodMetadata build(Method method) {
        return new MethodMetadata(method.getName(), method.getReturnType(),
            method.getParameterTypes(), method.getModifiers());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodMetadata that = (MethodMetadata) o;
        return Objects.equals(name, that.name) &&
                Arrays.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(params);
        return result;
    }
}
