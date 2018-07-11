package com.joe.utils.secure;

/**
 * 加密工具
 */
public interface CipherUtil {
    /**
     * 加密
     *
     * @param content 要加密的内容
     * @return 加密后的数据
     */
    String encrypt(String content);

    /**
     * 加密
     *
     * @param content 要加密的内容
     * @return 加密后的数据
     */
    byte[] encrypt(byte[] content);

    /**
     * 解密
     *
     * @param content 要解密的密文
     * @return 解密后的数据
     */
    String decrypt(String content);

    /**
     * 解密
     *
     * @param byteContent 要解密的内容
     * @return 解密后的数据
     */
    byte[] decrypt(byte[] byteContent);
}
