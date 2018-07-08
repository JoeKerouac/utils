package com.joe.utils.secure;

import com.joe.utils.common.IOUtils;
import com.joe.utils.common.StringUtils;
import com.joe.utils.pool.ObjectPool;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用private key加密
 *
 * @author joe
 * @version 2018.06.28 15:52
 */
@Slf4j
public class RSA implements Encipher {
    private static final IBase64 BASE_64 = new IBase64();
    private static final Map<String, ObjectPool<Signature>> CACHE = new HashMap<>();
    /**
     * 私钥ID
     */
    private String privateId;
    /**
     * 公钥ID
     */
    private String publicId;

    /**
     * 默认构造器
     *
     * @param privateKey 私钥
     * @param publicKey  对应的公钥，可以为空，为空时不能校验
     * @param rsaType    rsa加密类型
     */
    public RSA(String privateKey, String publicKey, RSAType rsaType) {
        this.privateId = (privateKey + rsaType.toString()).intern();
        ObjectPool<Signature> privatePool = CACHE.get(this.privateId);
        if (privatePool == null) {
            synchronized (privateId) {
                privatePool = CACHE.get(privateId);
                if (privatePool == null) {
                    privatePool = new ObjectPool(() -> buildPrivateSignature(privateKey, rsaType));
                    CACHE.put(privateId, privatePool);
                }
            }
        }

        this.publicId = (publicKey + rsaType.toString()).intern();
        ObjectPool<Signature> publicPool = CACHE.get(this.publicId);
        if (publicPool == null) {
            synchronized (publicId) {
                publicPool = CACHE.get(publicId);
                if (publicPool == null) {
                    publicPool = new ObjectPool(() -> buildPublicSignature(publicKey, rsaType));
                    CACHE.put(publicId, publicPool);
                }
            }
        }
    }

    /**
     * 使用指定私钥和RSA加密类型获取RSA加密器
     *
     * @param privateKey 私钥
     * @param rsaType    RSA加密类型
     * @return RSA加密器
     */
    private Signature buildPrivateSignature(String privateKey, RSAType rsaType) {
        log.debug("构建私钥加密器");
        try {
            PrivateKey priKey = getPrivateKeyFromPKCS8("RSA", new ByteArrayInputStream(privateKey.getBytes()));
            Signature signature = Signature.getInstance(rsaType.toString());
            signature.initSign(priKey);
            return signature;
        } catch (Exception e) {
            //不会发生这种情况
            throw new SecureException("创建RSA加密器失败", e);
        }
    }

    /**
     * 使用指定公钥和RSA加密类型获取RSA验证器
     *
     * @param publicKey 公钥
     * @param rsaType   RSA加密类型
     * @return RSA验证器
     */
    private Signature buildPublicSignature(String publicKey, RSAType rsaType) {
        log.debug("构建公钥验证器");
        try {
            PublicKey pubKey = getPublicKeyFromX509("RSA", new ByteArrayInputStream(publicKey.getBytes()));
            Signature signature = Signature.getInstance(rsaType.toString());
            signature.initVerify(pubKey);
            return signature;
        } catch (Exception e) {
            //不会发生这种情况
            throw new SecureException("创建RSA加密器失败", e);
        }
    }

    @Override
    public String encrypt(String content) {
        return new String(encrypt(content.getBytes()));
    }

    @Override
    public byte[] encrypt(byte[] content) {
        try {
            ObjectPool.PoolObjectHolder<Signature> holder = CACHE.get(privateId).get();
            Signature signature = holder.get();
            signature.update(content);
            byte[] result = BASE_64.encrypt(signature.sign());
            holder.close();
            return result;
        } catch (Exception e) {
            throw new SecureException("加密失败", e);
        }
    }

    /**
     * 使用公钥校验私钥加密的内容
     *
     * @param content 原文
     * @param data    私钥加密的数据
     * @return 返回true表示校验成功
     */
    public boolean check(String content, String data) {
        return check(content.getBytes(), data.getBytes());
    }

    /**
     * 使用公钥校验私钥加密的内容
     *
     * @param content 原文
     * @param data    私钥加密的数据（使用BASE64加密过的数据）
     * @return 返回true表示校验成功
     */
    public boolean check(byte[] content, byte[] data) {
        try {
            ObjectPool.PoolObjectHolder<Signature> holder = CACHE.get(publicId).get();
            Signature signature = holder.get();
            signature.update(content);
            boolean result = signature.verify(BASE_64.decrypt(data));
            holder.close();
            return result;
        } catch (Exception e) {
            throw new SecureException("加密失败", e);
        }
    }

    @Override
    public String decrypt(String content) {
        throw new SecureException("RSA不支持解密");
    }

    @Override
    public byte[] decrypt(byte[] byteContent) {
        throw new SecureException("RSA不支持解密");
    }

    /**
     * 从PKCS8格式的文件中获取私钥
     *
     * @param algorithm 加密算法名称
     * @param ins       PKCS8文件的输入流
     * @return 私钥
     * @throws Exception Exception
     */
    private static PrivateKey getPrivateKeyFromPKCS8(String algorithm, InputStream ins) throws Exception {
        if (ins == null || StringUtils.isEmpty(algorithm)) {
            return null;
        }
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        byte[] encodedKey = IOUtils.read(ins);
        encodedKey = BASE_64.decrypt(encodedKey);
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
    }

    /**
     * 从X509格式的文件中获取public key
     *
     * @param algorithm 加密算法名称
     * @param ins       X509格式的public key文件的输入流
     * @return 公钥
     * @throws Exception Exception
     */
    private static PublicKey getPublicKeyFromX509(String algorithm, InputStream ins) throws Exception {
        if (ins == null || StringUtils.isEmpty(algorithm)) {
            return null;
        }
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        byte[] encodedKey = IOUtils.read(ins);
        encodedKey = BASE_64.decrypt(encodedKey);
        return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
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
}
