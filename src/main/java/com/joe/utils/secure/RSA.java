package com.joe.utils.secure;

import com.joe.utils.common.IOUtils;
import com.joe.utils.common.StringUtils;
import com.joe.utils.pool.ObjectPool;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * RSA加密
 *
 * @author joe
 * @version 2018.06.28 15:52
 */
public class RSA implements Encipher {
    private static final IBase64 BASE_64 = new IBase64();
    private static final Map<String, ObjectPool<Signature>> CACHE = new HashMap<>();
    private String privateKey;
    private RSAType rsaType;
    private String id;

    /**
     * 默认构造器
     *
     * @param privateKey 私钥
     * @param rsaType    rsa加密类型
     */
    public RSA(String privateKey, RSAType rsaType) {
        this.privateKey = privateKey;
        this.rsaType = rsaType;
        this.id = (privateKey + rsaType.toString()).intern();

        ObjectPool<Signature> pool = CACHE.get(this.id);
        if (pool == null) {
            synchronized (id) {
                pool = CACHE.get(id);
                if (pool == null) {
                    build(privateKey, rsaType);
                    pool = new ObjectPool(() -> build(privateKey, rsaType));
                    CACHE.put(id, pool);
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
    private Signature build(String privateKey, RSAType rsaType) {
        try {
            PrivateKey priKey = getPrivateKeyFromPKCS8("RSA", new ByteArrayInputStream(privateKey
                    .getBytes()));
            Signature signature = Signature.getInstance(rsaType.toString());
            signature.initSign(priKey);
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
            ObjectPool.PoolObjectHolder<Signature> holder = CACHE.get(id).get();
            Signature signature = holder.get();
            signature.update(content);
            byte[] result = BASE_64.encrypt(signature.sign());
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
    public static PrivateKey getPrivateKeyFromPKCS8(String algorithm, InputStream ins) throws Exception {
        if (ins == null || StringUtils.isEmpty(algorithm)) {
            return null;
        }
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        byte[] encodedKey = IOUtils.read(ins);
        encodedKey = BASE_64.decrypt(encodedKey);
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
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
        if (obj instanceof RSA) {
            RSA rsa = (RSA) obj;
            return rsa.id.equals(this.id);
        }
        return false;
    }

    /**
     * RSA加密类型
     */
    public enum RSAType {
        SHA256WithRSA, SHA1WithRSA,
    }
}
