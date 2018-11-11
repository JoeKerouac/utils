package com.joe.utils.proxy;

import java.lang.reflect.Method;

/**
 * 拦截点，拦截到方法后执行拦截点
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月08日 20:43 JoeKerouac Exp $
 */
public interface Interception {

    /**
     * 拦截点执行，可以使用invoker对父类方法发起调用
     * @param params 方法调用参数
     * @param invoker 父类方法调用（可能为null，为null时表示无法调用父类方法）
     * @param method 拦截的方法
     * @return 拦截点执行结果
     */
    Object invoke(Object[] params, Invoker invoker, Method method);
}
