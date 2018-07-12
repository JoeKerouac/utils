package com.joe.utils.secure;

import java.security.Key;

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
     * @param content 要解密的内容
     * @return 解密后的数据
     */
    byte[] decrypt(byte[] content);

    /**
     * 获取私钥
     *
     * @return 私钥
     */
    Key getPrivateKey();

    /**
     * 获取公钥
     *
     * @return 公钥
     */
    Key getPublicKey();

    /**
     * ID，对于key相同的Cipher，ID应该也相同
     *
     * @return ID
     */
    String getId();

    /**
     * 获取算法
     *
     * @return 算法
     */
    Algorithms getAlgorithms();

    /**
     * 算法列表
     */
    enum Algorithms {
        RSA, AES, DES
    }
}
