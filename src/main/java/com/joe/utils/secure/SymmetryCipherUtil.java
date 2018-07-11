package com.joe.utils.secure;

import java.security.Key;

/**
 * 对称加密工具
 *
 * @author joe
 * @version 2018.07.11 18:47
 */
public class SymmetryCipherUtil extends AbstractCipherUtil {

    /**
     * 默认构造器
     *
     * @param algorithms 算法，支持DES和AES
     * @param password   密码
     * @param keySize    密码大小
     */
    public SymmetryCipherUtil(Algorithms algorithms, String password, int keySize) {
        super(algorithms, password, keySize);
    }

    @Override
    protected <T extends Key> CipherHolder<T> build(Algorithms algorithms, String privateKey, String publicKey, int
            keySize) {
        return null;
    }
}
