package com.joe.utils.proxy;

import java.lang.reflect.Method;

/**
 * 方法拦截器
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月07日 11:20 JoeKerouac Exp $
 */
public interface ProxyMethodFilter {

    /**
     * 将指定方法代理到另外的一个方法
     * 
     * @param method
     *            指定方法
     * @return 代理方法，返回null表示不代理该方法
     */
    Interception filter(Method method);
}
