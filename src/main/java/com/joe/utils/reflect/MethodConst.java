package com.joe.utils.reflect;

import java.lang.reflect.Method;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年02月23日 16:23 JoeKerouac Exp $
 */
public final class MethodConst {
    /**
     * String的equals方法
     */
    public static final Method EQUALAS_METHOD = ReflectUtil.getMethod(String.class, "equals", Object.class);

    /**
     * String的format方法
     */
    public static final Method FORMAT_METHOD =
        ReflectUtil.getMethod(String.class, "format", String.class, Object[].class);
}
