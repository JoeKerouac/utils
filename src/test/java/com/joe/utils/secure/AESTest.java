package com.joe.utils.secure;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author joe
 * @version 2018.07.11 16:46
 */
public class AESTest {
    AES aes ;
    String data = "这是要加密的数据，测试一下";

    @Before
    public void init() {
        aes = new AES("这个是密码");
    }

    @Test
    public void doCipher() {
        Assert.assertTrue(data.equals(aes.decrypt(aes.encrypt(data))));
    }
}
