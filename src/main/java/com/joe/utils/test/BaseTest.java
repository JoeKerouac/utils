package com.joe.utils.test;

import java.util.concurrent.Callable;

import com.joe.utils.function.CustomFunction;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月21日 18:37 JoeKerouac Exp $
 */
public abstract class BaseTest {
    private boolean skipAll = false;

    /**
     * 初始化方法，执行run前会自动执行
     */
    protected void init() {

    }

    /**
     * 销毁方法，执行run后会自动执行
     */
    protected void destroy() {

    }

    /**
     * 运行用例
     * 
     * @param function
     */
    protected void runCase(CustomFunction function) {
        if (skipAll) {
            return;
        }
        init();
        try {
            function.run();
        } catch (Throwable e) {
            throw new TestException(e);
        } finally {
            destroy();
        }
    }

    protected <T> T runCase(Callable<T> callable) {
        if (skipAll) {
            return null;
        }
        init();
        try {
            return callable.call();
        } catch (Throwable e) {
            throw new TestException(e);
        } finally {
            destroy();
        }
    }

    /**
     * 跳过用例执行
     * 
     * @param skipAll
     *            true表示跳过所有用例执行
     */
    protected void skipAll(boolean skipAll) {
        this.skipAll = skipAll;
    }
}
