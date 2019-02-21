package com.joe.utils.reflect;


/**
 * 动态类型ClassLoader
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年02月21日 19:51 JoeKerouac Exp $
 */
public abstract class DynamicClassLoader extends ClassLoader {

    public DynamicClassLoader() {

    }

    public DynamicClassLoader(ClassLoader parent) {
        super(parent);
    }



    /**
     * 根据bytecode生成class对象
     * @param name class对象名
     * @param b bytecode
     * @param begin byte code数组起始位置
     * @param len byte code数据长度
     * @return class对象
     */
    public abstract <T> Class<T> buildClass(String name, byte[] b, int begin, int len);
}
