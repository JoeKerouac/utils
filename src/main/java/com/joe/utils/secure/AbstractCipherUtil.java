package com.joe.utils.secure;

import com.joe.utils.pool.ObjectPool;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.crypto.Cipher;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

/**
 * 加密工具类顶级类
 *
 * @author joe
 * @version 2018.07.11 18:32
 */
public abstract class AbstractCipherUtil {
    private static final Map<String, ObjectPool<CipherHolder<?>>> CACHE = new HashMap<>();

    protected AbstractCipherUtil(String privateKey, String publicKey) {
        this(Algorithms.RSA, privateKey, publicKey, 0);
    }

    protected AbstractCipherUtil(Algorithms algorithms, String password, int keySize) {
        this(algorithms, password, password, keySize);
    }

    public AbstractCipherUtil(Algorithms algorithms, String privateKey, String publicKey, int keySize) {
        if (privateKey == publicKey && algorithms != Algorithms.DES && algorithms != Algorithms.AES) {
            throw new SecureException("只有DES和AES支持对称加密");
        }

        String id = (privateKey + ":" + publicKey + ":" + keySize).intern();
        if (CACHE.get(id) == null) {
            synchronized (id) {
                if (CACHE.get(id) == null) {
                    CACHE.put(id, new ObjectPool<>(() -> build(algorithms, privateKey, publicKey, keySize)));
                }
            }
        }
    }

    /**
     * 根据指定信息构建CipherHolder
     *
     * @param algorithms 算法名
     * @param privateKey 私钥
     * @param publicKey  公钥
     * @param keySize    key大小
     * @param <T>        key的实际类型
     * @return CipherHolder
     */
    protected abstract <T extends Key> CipherHolder<T> build(Algorithms algorithms, String privateKey, String publicKey,
                                                             int keySize);

    /**
     * 算法列表
     */
    enum Algorithms {
        RSA, AES, DES
    }

    /**
     * Cipher持有者
     *
     * @param <T> 密钥类型
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    protected static class CipherHolder<T extends Key> {
        /**
         * 加密器
         */
        private Cipher encrypt;
        /**
         * 解密器
         */
        private Cipher decrypt;
        /**
         * 公钥（对称加密中公钥私钥相同）
         */
        private T publicKey;
        /**
         * 私钥（对称加密中公钥私钥相同）
         */
        private T privateKey;
    }
}
