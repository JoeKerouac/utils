package com.joe.utils.proxy.bytebuddy;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import com.joe.utils.proxy.Interception;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

/**
 * 方法拦截器
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月09日 11:41 JoeKerouac Exp $
 */
public class GeneralInterceptor {

    private final Interception interception;

    public GeneralInterceptor(Interception interception) {
        this.interception = interception;
    }

    /**
     * 拦截有实现的方法
     * @param params 调用方法的参数
     * @param method 被拦截的方法
     * @param callable 父类调用
     * @return 执行结果
     */
    @RuntimeType
    public Object interceptClass(@AllArguments Object[] params,
                                   @Origin Method method, @SuperCall Callable<Object> callable) {
        return interception.invoke(params, callable::call, method);
    }

    /**
     * 拦截抽象方法
     * @param params 调用方法的参数
     * @param method 被拦截的方法
     * @return 执行结果
     */
    @RuntimeType
    public Object interceptInterface(@AllArguments Object[] params, @Origin Method method) {
        return interception.invoke(params, null, method);
    }
}
