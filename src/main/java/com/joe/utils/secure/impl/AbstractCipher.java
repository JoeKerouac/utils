package com.joe.utils.secure.impl;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import com.joe.utils.pool.ObjectPoolImpl;
import com.joe.utils.pool.PooledObject;
import com.joe.utils.secure.CipherUtil;
import com.joe.utils.secure.exception.SecureException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 加密工具辅助类
 *
 * @author joe
 * @version 2018.07.11 18:32
 */
@Slf4j
public abstract class AbstractCipher implements CipherUtil {
    private static final Map<String, ObjectPoolImpl<CipherHolder>> CACHE = new ConcurrentHashMap<>();
    private String id;
    private Algorithms algorithms;
    private Key priKey;
    private Key pubKey;

    AbstractCipher(String id, Algorithms algorithms, Key priKey, Key pubKey) {
        this.id = id.intern();
        this.algorithms = algorithms;
        this.priKey = priKey;
        this.pubKey = pubKey;

        CACHE.computeIfAbsent(this.id, key -> {
            ObjectPoolImpl<CipherHolder> pool = new ObjectPoolImpl<>(this::build);
            // 快速验证
            pool.get().close();
            return pool;
        });
    }

    /**
     * 根据指定信息构建CipherHolder
     *
     * @return CipherHolder
     */
    private CipherHolder build() {
        Algorithms algorithms = getAlgorithms();
        Key priKey = getPrivateKey();
        Key pubKey = getPublicKey();
        log.debug("构建key成功，开始构建Cipher");
        try {
            Cipher encrypt = Cipher.getInstance(algorithms.toString());
            encrypt.init(Cipher.ENCRYPT_MODE, pubKey);

            Cipher decrypt = Cipher.getInstance(algorithms.toString());
            decrypt.init(Cipher.DECRYPT_MODE, priKey);
            log.debug("Cipher构建成功");
            return new CipherHolder(algorithms, encrypt, decrypt, pubKey, priKey);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            throw new SecureException("构建CipherHolder[" + algorithms + "]失败", e);
        }
    }

    /**
     * 加密指定数组
     *
     * @param holder
     *            CipherHolder
     * @param data
     *            要加密的数据
     * @return 加密后的数据（有可能会对结果编码）
     */
    protected abstract byte[] encrypt(CipherHolder holder, byte[] data);

    /**
     * 解密指定数组
     *
     * @param holder
     *            CipherHolder
     * @param data
     *            要解密的数据
     * @return 解密后的数据
     */
    protected abstract byte[] decrypt(CipherHolder holder, byte[] data);

    @Override
    public String encrypt(String content) {
        return new String(encrypt(content.getBytes()));
    }

    @Override
    public byte[] encrypt(byte[] content) {
        try (PooledObject<CipherHolder> holder = CACHE.get(id).get()) {
            return encrypt(holder.get(), content);
        }
    }

    @Override
    public String decrypt(String content) {
        return new String(decrypt(content.getBytes()));
    }

    @Override
    public byte[] decrypt(byte[] content) {
        try (PooledObject<CipherHolder> holder = CACHE.get(id).get()) {
            return decrypt(holder.get(), content);
        }
    }

    @Override
    public Key getPrivateKey() {
        return priKey;
    }

    @Override
    public Key getPublicKey() {
        return pubKey;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Algorithms getAlgorithms() {
        return algorithms;
    }

    /**
     * Cipher持有者
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    protected static class CipherHolder {
        /**
         * 算法
         */
        private Algorithms algorithms;
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
        private Key publicKey;
        /**
         * 私钥（对称加密中公钥私钥相同）
         */
        private Key privateKey;
    }
}
