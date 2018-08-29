package com.joe.utils.common;

import java.io.UnsupportedEncodingException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author joe
 * @version 2018.08.29 11:47
 */
public class StringUtilsTest {
    private String data;

    @Before
    public void init() throws UnsupportedEncodingException {
        data = new String("sdafsad撒地方深大".getBytes(), "GBK");
    }

    @Test
    public void doCopy() {
        Assert.assertTrue("***".equals(StringUtils.copy("*", 3)));
    }
}
