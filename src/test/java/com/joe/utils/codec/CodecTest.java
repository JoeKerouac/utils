package com.joe.utils.codec;

import java.util.Arrays;

import com.joe.utils.reflect.ReflectUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author joe
 * @version 2018.08.09 14:29
 */
public class CodecTest {
    static byte[] datas;
    static String hex;
    static {
        datas = new byte[] { 12, 56, 123, 1, 0, -45, 36, -123, -48, 94, 78, 53, 48, 12, 75, 48, 11,
                             10, 34, 56, 91, 61, 43, 81, 61 };
        hex = "0c387b0100d32485d05e4e35300c4b300b0a22385b3d2b513d";
    }

    @Test
    public void doHexEncodeTest() {
        Assert.assertEquals(new String(Hex.encodeHex(datas, true)), hex);
    }

    @Test
    public void doHexDecodeTest() {
        Assert.assertTrue(Arrays.equals(datas, Hex.decodeHex(hex)));
        Assert.assertTrue(
            Arrays.equals(datas, Hex.decodeHex((char[]) ReflectUtil.getFieldValue(hex, "value"))));
    }

    @Test
    public void doBase64Test() {
        String text = "测试文本";
        Assert.assertEquals(IBase64.decrypt(IBase64.encrypt(text)), text);
        Assert.assertTrue(
            Arrays.equals(text.getBytes(), IBase64.decrypt(IBase64.encrypt(text.getBytes()))));
    }
}
