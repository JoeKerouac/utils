package com.joe.utils.secure;

import com.joe.utils.codec.IBase64;
import com.joe.utils.common.IOUtils;
import com.joe.utils.common.StringUtils;
import com.joe.utils.pool.ObjectPool;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * RSA加密，可以使用公钥加密，私钥解密或者私钥签名，公钥验签
 *
 * @author joe
 * @version 2018.06.28 15:52
 */
@Slf4j
public class RSA {
    private static final IBase64 BASE_64 = new IBase64();
    private static final Map<String, ObjectPool<RSAComponentHolder<?>>> CACHE = new HashMap<>();
    /**
     * 私钥ID
     */
    private final String privateId;
    /**
     * 公钥ID
     */
    private final String publicId;

    /**
     * 默认构造器
     *
     * @param privateKey  私钥
     * @param publicKey   对应的公钥，可以为空，为空时不能校验
     * @param rsaSignType rsa加密类型
     */
    public RSA(String privateKey, String publicKey, RSASignType rsaSignType) {
        this.privateId = (privateKey + rsaSignType.toString()).intern();
        ObjectPool<RSAComponentHolder<?>> privatePool = CACHE.get(this.privateId);
        if (privatePool == null) {
            synchronized (privateId) {
                privatePool = CACHE.get(privateId);
                if (privatePool == null) {
                    privatePool = new ObjectPool<>(() -> buildPrivateSignature(privateKey, rsaSignType));
                    CACHE.put(privateId, privatePool);
                }
            }
        }

        this.publicId = (publicKey + rsaSignType.toString()).intern();
        ObjectPool<RSAComponentHolder<?>> publicPool = CACHE.get(this.publicId);
        if (publicPool == null) {
            synchronized (publicId) {
                publicPool = CACHE.get(publicId);
                if (publicPool == null) {
                    publicPool = new ObjectPool<>(() -> buildPublicSignature(publicKey, rsaSignType));
                    CACHE.put(publicId, publicPool);
                }
            }
        }
    }

    /**
     * 使用私钥签名
     *
     * @param content 要签名的数据
     * @return 签名结果（会对签名结果做BASE64 encode处理）
     */
    public String sign(String content) {
        return new String(sign(content.getBytes()));
    }

    /**
     * 使用私钥签名
     *
     * @param content 要签名的数据
     * @return 签名结果（会对签名结果做BASE64 encode处理）
     */
    public byte[] sign(byte[] content) {
        try (ObjectPool.PoolObjectHolder<RSAComponentHolder<?>> holder = CACHE.get(privateId).get()) {
            Signature signature = holder.get().getSignature();
            signature.update(content);
            return BASE_64.encrypt(signature.sign());
        } catch (Exception e) {
            throw new SecureException("加密失败", e);
        }
    }

    /**
     * 使用公钥校验签名
     *
     * @param content 原文
     * @param data    签名数据
     * @return 返回true表示校验成功
     */
    public boolean checkSign(String content, String data) {
        return checkSign(content.getBytes(), data.getBytes());
    }

    /**
     * 使用公钥校验签名
     *
     * @param content 原文
     * @param data    签名数据（BASE64 encode过的）
     * @return 返回true表示校验成功
     */
    public boolean checkSign(byte[] content, byte[] data) {
        try (ObjectPool.PoolObjectHolder<RSAComponentHolder<?>> holder = CACHE.get(publicId).get()) {
            Signature signature = holder.get().getSignature();
            signature.update(content);
            return signature.verify(BASE_64.decrypt(data));
        } catch (Exception e) {
            throw new SecureException("加密失败", e);
        }
    }

    /**
     * 使用指定私钥和RSA加密类型获取RSA加密器
     *
     * @param privateKey  私钥
     * @param rsaSignType RSA加密类型
     * @return RSA加密器
     */
    public static RSAComponentHolder<RSAPrivateKey> buildPrivateSignature(String privateKey, RSASignType rsaSignType) {
        log.debug("构建私钥加密器");
        try {
            RSAPrivateKey priKey = (RSAPrivateKey) KeyTools.getPrivateKeyFromPKCS8("RSA", new ByteArrayInputStream(privateKey
                    .getBytes()));
            Signature signature = Signature.getInstance(rsaSignType.toString());
            signature.initSign(priKey);

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, priKey);
            return new RSAComponentHolder<>(signature, cipher, priKey);
        } catch (Exception e) {
            //不会发生这种情况
            throw new SecureException("创建RSA加密器失败", e);
        }
    }

    /**
     * 使用指定公钥和RSA加密类型获取RSA验证器
     *
     * @param publicKey   公钥
     * @param rsaSignType RSA加密类型
     * @return RSA验证器
     */
    public static RSAComponentHolder<RSAPublicKey> buildPublicSignature(String publicKey, RSASignType rsaSignType) {
        log.debug("构建公钥验证器");
        try {
            RSAPublicKey pubKey = (RSAPublicKey) KeyTools.getPublicKeyFromX509("RSA", new ByteArrayInputStream(publicKey
                    .getBytes()));
            Signature signature = Signature.getInstance(rsaSignType.toString());
            signature.initVerify(pubKey);

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);

            return new RSAComponentHolder<>(signature, cipher, pubKey);
        } catch (Exception e) {
            //不会发生这种情况
            throw new SecureException("创建RSA加密器失败", e);
        }
    }

    @Override
    public int hashCode() {
        return this.privateId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof RSA) {
            RSA rsa = (RSA) obj;
            return rsa.privateId.equals(this.privateId);
        }
        return false;
    }

    /**
     * RSA组件持有者
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class RSAComponentHolder<T extends Key> {
        private Signature signature;
        private Cipher cipher;
        private T key;
    }
}
