package com.joe.utils.secure;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.joe.utils.codec.IBase64;
import com.joe.utils.common.IOUtils;
import com.joe.utils.common.string.StringUtils;
import com.joe.utils.secure.exception.SecureException;
import com.joe.utils.secure.impl.AbstractCipher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Key工具
 *
 * @author joe
 * @version 2018.07.11 21:19
 */
@Slf4j
public class KeyTools {

    /**
     * 构建RSA密钥对
     *
     * @param keySize
     *            keySize（必须大于等于512）
     * @return RSA密钥对
     */
    public static KeyHolder buildRSAKey(int keySize) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(keySize);
            KeyPair keyPair = generator.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();
            return new KeyHolder(privateKey, publicKey);
        } catch (NoSuchAlgorithmException e) {
            throw new SecureException("当前系统没有提供生成RSA密钥对的算法", e);
        }
    }

    /**
     * 从PKCS8格式的文件中获取私钥
     *
     * @param algorithm
     *            加密算法名称
     * @param ins
     *            PKCS8文件的输入流
     * @return 私钥
     */
    public static PrivateKey getPrivateKeyFromPKCS8(String algorithm, InputStream ins) {
        if (ins == null || StringUtils.isEmpty(algorithm)) {
            return null;
        }
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            byte[] encodedKey = IOUtils.read(ins);
            encodedKey = IBase64.decrypt(encodedKey);
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            throw new SecureException("构建[" + algorithm + "]私钥失败", e);
        }
    }

    /**
     * 从X509格式的文件中获取public key
     *
     * @param algorithm
     *            加密算法名称
     * @param ins
     *            X509格式的public key文件的输入流
     * @return 公钥
     */
    public static PublicKey getPublicKeyFromX509(String algorithm, InputStream ins) {
        if (ins == null || StringUtils.isEmpty(algorithm)) {
            return null;
        }

        try {
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            byte[] encodedKey = IOUtils.read(ins);
            encodedKey = IBase64.decrypt(encodedKey);
            return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            throw new SecureException("构建[" + algorithm + "]私钥失败", e);
        }
    }

    /**
     * 使用指定password构建对称加密的Key，即使seed相同构建出来的key也会不同，因为seed仅用于生成随机数种 子，keySize有一定限制，详情参照README
     *
     * @param algorithm
     *            算法名称，当前仅支持AES和DES
     * @param seed
     *            用来生成随机数的种子
     * @param keySize
     *            keySize
     * @return 对称加密的key
     */
    public static SecretKey buildKey(AbstractCipher.Algorithms algorithm, String seed, int keySize) {
        log.debug("开始构建对称加密[{}]的Key，Keysize为：[{}]", algorithm, keySize);
        String name = algorithm.name();
        try {
            KeyGenerator kgen = KeyGenerator.getInstance(name);
            kgen.init(keySize, new SecureRandom(seed.getBytes()));
            return kgen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new SecureException("构建[" + algorithm + "]的key失败，keySize是：" + keySize, e);
        }
    }

    /**
     * 使用指定keySpec构建对称加密的key
     *
     * @param algorithm
     *            算法名称，当前仅支持AES和DES
     * @param keySpec
     *            keySpec，多次调用该方法生成的key等效
     * @return 对称加密的key
     */
    public static SecretKey buildKey(AbstractCipher.Algorithms algorithm, byte[] keySpec) {
        return new SecretKeySpec(keySpec, algorithm.name());
    }

    @Data
    @AllArgsConstructor
    public static class KeyHolder {
        private PrivateKey privateKey;
        private PublicKey publicKey;
    }
}
