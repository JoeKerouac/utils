package com.joe.utils.secure;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;

public class MD5 implements Encipher {
    /**
     * 获取文本的MD5（注：如果参数不是以UTF8格式编码的使用该实现会出错，请自行使用getByte方法获取字符串的byte[]格式，然
     * 后调用{@link #encrypt(byte[])}
     *
     * @param content 要加密的内容
     * @return 加密后的内容
     */
    public String encrypt(String content) {
        return DigestUtils.md5Hex(content);
    }

    public byte[] encrypt(byte[] content) {
        return DigestUtils.md5Hex(content).getBytes();
    }

    public String decrypt(String content) {
        return null;
    }

    public byte[] decrypt(byte[] byteContent) {
        return null;
    }


    /**
     * 计算MD5并返回128位（16byte）的结果（注：MD5的结果是以16进制数呈现的，一个16进制数4bit，32个刚好128bit，16byte）
     *
     * @param content 要加密的数据
     * @return 加密后的原始结果（没有将一个byte拆分成两个16进制数）
     */
    public byte[] encrypt16(byte[] content) {
        return DigestUtils.md5(content);
    }

    /**
     * 计算MD5并返回128位（16byte）的结果（注：MD5的结果是以16进制数呈现的，一个16进制数4bit，32个刚好128bit，16byte）
     *
     * @param content 要加密的数据的输入流
     * @return 加密后的原始结果（没有将一个byte拆分成两个16进制数）
     * @throws IOException IO异常
     */
    public byte[] encrypt16(InputStream content) throws IOException {
        return DigestUtils.md5(content);
    }
}
