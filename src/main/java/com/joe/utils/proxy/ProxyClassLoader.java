package com.joe.utils.proxy;

/**
 * 代理客户端ClassLoader
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月08日 11:42 JoeKerouac Exp $
 */
public class ProxyClassLoader extends ClassLoader {

    public ProxyClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * 构建class
     * 
     * @param data
     *            class数据
     * @return 构建的Class
     */
    public Class<?> buildClass(byte[] data) {
        return super.defineClass(null, data, 0, data.length);
    }
}
