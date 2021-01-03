package com.joe.utils.reflect.clazz;

import java.io.InputStream;

/**
 * class提供者
 *
 * @author JoeKerouac
 * @version 2019年09月29日 11:19
 */
public interface ClassProvider {

    /**
     * 根据className查找class的输入流
     * 
     * @param className
     *            className
     * @return class的输入流
     * @throws ClassNotFoundException
     *             找不到时应该抛出该异常
     */
    InputStream findClassStream(String className) throws ClassNotFoundException;
}
