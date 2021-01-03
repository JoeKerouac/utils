package com.joe.utils.secure;

import org.junit.Assert;

/**
 * @author joe
 * @version 2018.07.11 21:55
 */
public class CipherUtilTest {
    private static String data = "这是测试加密字符串";

    /**
     * 检查加密是否能用
     *
     * @param cipher
     *            加密器
     */
    public static void checkCipher(CipherUtil cipher) {
        Assert.assertEquals(data, cipher.decrypt(cipher.encrypt(data)));
    }
}
