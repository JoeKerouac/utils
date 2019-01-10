package com.joe.utils.proxy;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年12月29日 16:24 JoeKerouac Exp $
 */
class ProxyUtil {

    /**
     * 转换classloader
     * @param loader 原classloader
     * @return 转换后的classloader
     */
    public static ProxyClassLoader convertClassloader(ClassLoader loader) {
        if (loader == null) {
            return ProxyClient.DEFAULT_LOADER;
        } else if (loader instanceof ProxyClassLoader) {
            return (ProxyClassLoader) loader;
        } else {
            return new ProxyClassLoader(loader);
        }
    }
}
