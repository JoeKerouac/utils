package com.joe.utils.secure.impl;

import java.io.ByteArrayInputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.joe.utils.codec.IBase64;
import com.joe.utils.pool.ObjectPoolImpl;
import com.joe.utils.pool.PooledObject;
import com.joe.utils.secure.KeyTools;
import com.joe.utils.secure.SignatureUtil;
import com.joe.utils.secure.exception.SecureException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 签名工具
 *
 * @author joe
 * @version 2018.06.28 15:52
 */
@Slf4j
public class SignatureUtilImpl implements SignatureUtil {
    private static final IBase64                                      BASE_64 = new IBase64();
    private static final Map<String, ObjectPoolImpl<SignatureHolder>> CACHE   = new ConcurrentHashMap<>();
    /**
     * ID
     */
    private final String                                              id;

    /**
     * 默认构造器
     *
     * @param privateKey 私钥
     * @param publicKey  对应的公钥，可以为空，为空时不能校验
     * @param algorithms rsa加密类型
     */
    private SignatureUtilImpl(String privateKey, String publicKey, Algorithms algorithms) {
        this.id = (privateKey + ":" + publicKey + ":" + algorithms.toString()).intern();

        CACHE.computeIfAbsent(this.id, id -> {
            ObjectPoolImpl<SignatureHolder> pool = new ObjectPoolImpl<>(
                () -> buildSignatureHolder(privateKey, publicKey, algorithms));
            //快速验证
            pool.get().close();
            return pool;
        });
    }

    /**
     * 构建一个SignatureUtil
     *
     * @param privateKey PKCS8文件格式的私钥
     * @param publicKey  X509格式的公钥
     * @param algorithms RSA加密类型
     * @return SignatureUtil
     */
    public static SignatureUtil buildInstance(String privateKey, String publicKey,
                                              Algorithms algorithms) {
        return new SignatureUtilImpl(privateKey, publicKey, algorithms);
    }

    /**
     * 使用私钥签名
     *
     * @param content 要签名的数据
     * @return 签名结果（会对签名结果做BASE64 encode处理）
     */
    @Override
    public String sign(String content) {
        return new String(sign(content.getBytes()));
    }

    /**
     * 使用私钥签名
     *
     * @param content 要签名的数据
     * @return 签名结果（会对签名结果做BASE64 encode处理）
     */
    @Override
    public byte[] sign(byte[] content) {
        try (PooledObject<SignatureHolder> holder = CACHE.get(id).get()) {
            Signature signature = holder.get().getSign();
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
    @Override
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
    @Override
    public boolean checkSign(byte[] content, byte[] data) {
        try (PooledObject<SignatureHolder> holder = CACHE.get(id).get()) {
            Signature signature = holder.get().getVerify();
            signature.update(content);
            return signature.verify(BASE_64.decrypt(data));
        } catch (Exception e) {
            throw new SecureException("加密失败", e);
        }
    }

    /**
     * 使用指定公钥和RSA加密类型获取RSA验证器
     *
     * @param privateKey PKCS8文件格式的私钥
     * @param publicKey  X509格式的公钥
     * @param algorithms RSA加密类型
     * @return RSA验证器
     */
    private static SignatureHolder buildSignatureHolder(String privateKey, String publicKey,
                                                        Algorithms algorithms) {
        log.debug("构建SignatureHolder");
        try {
            log.debug("构建公钥以及验签器");
            RSAPublicKey pubKey = (RSAPublicKey) KeyTools.getPublicKeyFromX509("RSA",
                new ByteArrayInputStream(publicKey.getBytes()));
            Signature verify = Signature.getInstance(algorithms.toString());
            verify.initVerify(pubKey);

            log.debug("构建私钥以及签名器");
            RSAPrivateKey priKey = (RSAPrivateKey) KeyTools.getPrivateKeyFromPKCS8("RSA",
                new ByteArrayInputStream(privateKey.getBytes()));
            Signature sign = Signature.getInstance(algorithms.toString());
            sign.initSign(priKey);

            log.debug("SignatureHolder构建成功");
            return new SignatureHolder(sign, verify, priKey, pubKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new SecureException("创建验签器[" + algorithms + "]失败", e);
        }
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SignatureUtilImpl) {
            SignatureUtilImpl signatureUtilImpl = (SignatureUtilImpl) obj;
            return signatureUtilImpl.id.equals(this.id);
        }
        return false;
    }

    /**
     * Signature持有者
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class SignatureHolder {
        private Signature     sign;
        private Signature     verify;
        private RSAPrivateKey signKey;
        private PublicKey     verifyKey;
    }
}
