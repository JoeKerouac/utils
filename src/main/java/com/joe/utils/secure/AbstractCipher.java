package com.joe.utils.secure;

import com.joe.utils.codec.IBase64;
import com.joe.utils.pool.ObjectPool;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * 加密工具辅助类
 *
 * @author joe
 * @version 2018.07.11 18:32
 */
@Slf4j
public abstract class AbstractCipher implements CipherUtil {
    private static final Map<String, ObjectPool<CipherHolder>> CACHE = new HashMap<>();
    protected static final IBase64 BASE_64 = new IBase64();
    private String id;

    /**
     * 非对称加密构造器
     *
     * @param algorithms 算法名称
     * @param privateKey PKCS8格式的私钥
     * @param publicKey  X509格式的公钥
     */
    protected AbstractCipher(Algorithms algorithms, String privateKey, String publicKey) {
        this(algorithms, privateKey, publicKey, 0);
    }

    /**
     * 对称加密
     *
     * @param algorithms 算法名称
     * @param password   密码，用来作为随机数种子
     * @param keySize    keySize
     */
    protected AbstractCipher(Algorithms algorithms, String password, int keySize) {
        this(algorithms, password, password, keySize);
    }

    private AbstractCipher(Algorithms algorithms, String privateKey, String publicKey, int keySize) {
        if (privateKey.equals(publicKey) && algorithms != Algorithms.DES && algorithms != Algorithms.AES) {
            throw new SecureException("只有DES和AES支持对称加密");
        }

        if (keySize < 0) {
            if (algorithms == Algorithms.AES) {
                keySize = 256;
            } else if (algorithms == Algorithms.DES) {
                keySize = 56;
            }
        }
        int size = keySize;

        this.id = (privateKey + ":" + publicKey + ":" + size).intern();
        if (CACHE.get(id) == null) {
            synchronized (id) {
                if (CACHE.get(id) == null) {
                    CACHE.put(id, new ObjectPool<>(() -> build(algorithms, privateKey, publicKey, size)));
                }
            }
        }
        //调用验证
        CACHE.get(id).get().close();
    }

    /**
     * 根据指定信息构建CipherHolder
     *
     * @param algorithms 算法名
     * @param privateKey 私钥
     * @param publicKey  公钥
     * @param keySize    key大小
     * @return CipherHolder
     */
    private CipherHolder build(Algorithms algorithms, String privateKey, String publicKey, int keySize) {
        log.debug("使用公钥[{}]、私钥[{}]构建算法[{}]对应的加密器，keySize为：[{}]", publicKey, privateKey, algorithms, keySize);
        Key priKey = buildPrivateKey(algorithms, privateKey, keySize);
        Key pubKey = buildPublicKey(algorithms, publicKey, keySize);
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
     * @param holder CipherHolder
     * @param data   要加密的数据
     * @return 加密后的数据（有可能会对结果编码）
     */
    protected abstract byte[] encrypt(CipherHolder holder, byte[] data);

    /**
     * 构建私钥
     *
     * @param algorithm  算法
     * @param privateKey 私钥
     * @param keySize    keySize
     * @return 私钥对象
     */
    protected abstract Key buildPrivateKey(Algorithms algorithm, String privateKey, int keySize);

    /**
     * 构建公钥
     *
     * @param algorithm 算法
     * @param publicKey 公钥
     * @param keySize   keySize
     * @return 公钥对象
     */
    protected abstract Key buildPublicKey(Algorithms algorithm, String publicKey, int keySize);

    /**
     * 解密指定数组
     *
     * @param holder CipherHolder
     * @param data   要解密的数据
     * @return 解密后的数据
     */
    protected abstract byte[] decrypt(CipherHolder holder, byte[] data);

    @Override
    public String encrypt(String content) {
        return new String(encrypt(content.getBytes()));
    }

    @Override
    public byte[] encrypt(byte[] content) {
        try (ObjectPool.PoolObjectHolder<CipherHolder> holder = CACHE.get(id).get()) {
            return encrypt(holder.get(), content);
        }
    }

    @Override
    public String decrypt(String content) {
        return new String(decrypt(content.getBytes()));
    }

    @Override
    public byte[] decrypt(byte[] content) {
        try (ObjectPool.PoolObjectHolder<CipherHolder> holder = CACHE.get(id).get()) {
            return decrypt(holder.get(), content);
        }
    }

    /**
     * 算法列表
     */
    enum Algorithms {
        RSA, AES, DES
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
