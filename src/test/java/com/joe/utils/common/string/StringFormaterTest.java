package com.joe.utils.common.string;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author JoeKerouac
 * @version 2019年08月15日 20:02
 */
public class StringFormaterTest {

    @Test
    public void doSimpleFormat() {
        Assert.assertEquals("你好啊:-JoeKerouac-", StringFormater.simpleFormat("你好啊:{1}{0}{1}", "JoeKerouac", "-"));
    }

    @Test
    public void doJsonFormat() {
        Assert.assertTrue(StringUtils.isNotEmpty(StringFormater.jsonFormat("{'1':'1','2':'2'}")));
    }
}
