package com.joe.utils.reflect.clazz;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import com.joe.utils.common.IOUtils;

/**
 * ClassUtils测试
 *
 * @author JoeKerouac
 * @version 2019年09月25日 10:32
 */
public class ClassUtilsTest {

    private static final JClassLoader LOADER = new JClassLoader(new String[] { ".*" },
        name -> ClassUtils.getClassAsStream(ClassUtils.loadClass(name)));

    @Test
    public void doTestGetClassAsStream() throws Exception {
        InputStream stream = ClassUtils.getClassAsStream(ClassUtils.class);
        Class<?> clazz = LOADER.defineClass(ClassUtils.class.getName(), IOUtils.read(stream));
        Assert.assertNotNull(clazz);
    }

    @Test
    public void doTestGetDefaultClassLoader() {
        Assert.assertNotNull(ClassUtils.getDefaultClassLoader());
    }

    @Test
    public void doTestReloadClass() {
        Class<?> clazz = ClassUtils.reloadClass(ClassUtilsTest.class, LOADER);
        Assert.assertNotNull(clazz);
    }
}
