package com.joe.utils.reflect;

/**
 * 调用分发接口
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年02月19日 19:01 JoeKerouac Exp $
 */
public interface InvokeDistribute {

    /**
     * {@link InvokeDistribute#invoke(String, String, String, Object[])}方法名
     */
    String DISTRIBUTE_METHOD_NAME = "invoke";

    /**
     * {@link InvokeDistribute#invoke(String, String, String, Object[])}方法的byte code说明
     */
    String DISTRIBUTE_METHOD_DESC = ByteCodeUtils
        .getMethodDesc(ReflectUtil.getMethod(InvokeDistribute.class, "invoke", String.class,
            String.class, String.class, Object[].class));

    Object invoke(String owner, String methodName, String desc, Object[] args);
}
