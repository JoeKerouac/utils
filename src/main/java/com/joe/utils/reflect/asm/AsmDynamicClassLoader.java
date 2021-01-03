package com.joe.utils.reflect.asm;

import com.joe.utils.reflect.DynamicClassLoader;

/**
 * ASM动态类的ClassLoader
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年02月19日 19:46 JoeKerouac Exp $
 */
public class AsmDynamicClassLoader extends DynamicClassLoader {

    public AsmDynamicClassLoader() {

    }

    public AsmDynamicClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<T> buildClass(String name, byte[] b, int begin, int len) {
        return (Class<T>)defineClass(name, b, begin, len);
    }
}
