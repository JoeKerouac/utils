package com.joe.utils.codec;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author joe
 * @version 2018.08.09 14:29
 */
public class CodecTest {
    byte[] datas;
    String hex;

    @Test
    public void doHex() {
        Assert.assertEquals(new String(Hex.encodeHex(datas, true)), hex);
    }

    @Test
    public void doBase64() {
        String text = "测试文本";
        Assert.assertEquals(IBase64.decrypt(IBase64.encrypt(text)), text);
        Assert.assertTrue(
            Arrays.equals(text.getBytes(), IBase64.decrypt(IBase64.encrypt(text.getBytes()))));
    }

    @Before
    public void init() {
        datas = new byte[] { 12, 56, 123, 1, 0, -45, 36, -123, -48, 94, 78, 53, 48, 12, 75, 48, 11,
                             10, 34, 56, 91, 61, 43, 81, 61 };
        hex = "0c387b0100d32485d05e4e35300c4b300b0a22385b3d2b513d";
    }
}
