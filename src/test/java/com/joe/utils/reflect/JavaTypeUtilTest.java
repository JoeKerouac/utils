package com.joe.utils.reflect;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年04月04日 09:55 JoeKerouac Exp $
 */
public class JavaTypeUtilTest {

    @Test
    @SuppressWarnings("unchecked")
    public void javaTypeTest() {
        Assert.assertNotNull(JavaTypeUtil.createMapType(HashMap.class, String.class, String.class));
        Assert.assertNotNull(JavaTypeUtil.createCollectionType(ArrayList.class, String.class));
        Assert.assertNotNull(JavaTypeUtil.createJavaType(String.class));
    }
}
