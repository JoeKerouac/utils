package com.joe.utils.secure;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 对称加密工具
 *
 * @author joe
 * @version 2018.07.11 18:47
 */
@Slf4j
public class SymmetryCipher extends AbstractCipher {
    /**
     * 默认采用AES算法，密钥空间大小为256
     *
     * @param password 密码，用来作为随机数的种子
     */
    public SymmetryCipher(String password) {
        this(Algorithms.AES, password);
    }

    /**
     * AES默认密钥空间大小为256，DES默认密钥空间大小为56
     *
     * @param algorithms 算法，当前仅支持AES和DES
     * @param password   密码，用来作为随机数的种子
     */
    public SymmetryCipher(Algorithms algorithms, String password) {
        this(algorithms, password, -1);
    }

    /**
     * 构造器
     *
     * @param algorithms 算法，支持DES和AES
     * @param password   密码，用来作为随机数的种子
     * @param keySize    密码大小（AES支持128、192、256，当使用大于128的空间时需要下载JCE Unlimited Strength Jurisdiction
     *                   Policy Files，地址为：
     *                   http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html ，DES只支持56）
     */
    public SymmetryCipher(Algorithms algorithms, String password, int keySize) {
        super(algorithms, password, keySize);
    }

    @Override
    protected Key buildPrivateKey(Algorithms algorithm, String privateKey, int keySize) {
        log.debug("开始构建对称加密[{}]的Key，Keysize为：[{}]", algorithm, keySize);
        String name = algorithm.toString();
        try {
            KeyGenerator kgen = KeyGenerator.getInstance(name);
            kgen.init(keySize, new SecureRandom(privateKey.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, name);
            return key;
        } catch (NoSuchAlgorithmException e) {
            throw new SecureException("构建[" + algorithm + "]的key失败，keySize是：" + keySize, e);
        }
    }

    @Override
    protected Key buildPublicKey(Algorithms algorithm, String publicKey, int keySize) {
        return buildPrivateKey(algorithm, publicKey, keySize);
    }

    @Override
    protected byte[] encrypt(CipherHolder holder, byte[] data) {
        try {
            return BASE_64.encrypt(holder.getEncrypt().doFinal(data));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new SecureException("加密算法[" + holder.getAlgorithms() + "]加密数据失败", e);
        }
    }

    @Override
    protected byte[] decrypt(CipherHolder holder, byte[] data) {
        try {
            return holder.getDecrypt().doFinal(BASE_64.decrypt(data));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new SecureException("解密算法[" + holder.getAlgorithms() + "]解密数据失败", e);
        }
    }
}
