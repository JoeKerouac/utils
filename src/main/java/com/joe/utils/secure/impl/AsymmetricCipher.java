package com.joe.utils.secure.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import com.joe.utils.codec.IBase64;
import com.joe.utils.common.IOUtils;
import com.joe.utils.secure.CipherUtil;
import com.joe.utils.secure.KeyTools;
import com.joe.utils.secure.exception.SecureException;

import lombok.extern.slf4j.Slf4j;

/**
 * 非对称加密（SignatureUtilImpl）
 *
 * @author joe
 * @version 2018.07.11 21:17
 */
@Slf4j
public class AsymmetricCipher extends AbstractCipher {
    private AsymmetricCipher(String id, Algorithms algorithms, PrivateKey privateKey,
                             PublicKey publicKey) {
        super(id, algorithms, privateKey, publicKey);
    }

    /**
     * 非对称加密构造器
     *
     * @param privateKey PKCS8格式的私钥（BASE64 encode过的）
     * @param publicKey  X509格式的公钥（BASE64 encode过的）
     * @return AsymmetricCipher
     */
    public static CipherUtil buildInstance(String privateKey, String publicKey) {
        return buildInstance(privateKey.getBytes(), publicKey.getBytes());
    }

    /**
     * 非对称加密构造器
     *
     * @param privateKey PKCS8格式的私钥（BASE64 encode过的）
     * @param publicKey  X509格式的公钥（BASE64 encode过的）
     * @return AsymmetricCipher
     */
    public static CipherUtil buildInstance(byte[] privateKey, byte[] publicKey) {
        PrivateKey priKey = KeyTools.getPrivateKeyFromPKCS8(Algorithms.RSA.name(),
            new ByteArrayInputStream(privateKey));
        PublicKey pubKey = KeyTools.getPublicKeyFromX509(Algorithms.RSA.name(),
            new ByteArrayInputStream(publicKey));
        return buildInstance(priKey, pubKey);
    }

    /**
     * 非对称加密构造器
     *
     * @param privateKey PKCS8格式的私钥
     * @param publicKey  X509格式的公钥
     * @return AsymmetricCipher
     */
    public static CipherUtil buildInstance(PrivateKey privateKey, PublicKey publicKey) {
        return new AsymmetricCipher(new String(IBase64.encrypt(privateKey.getEncoded())) + ":"
                                    + new String(IBase64.encrypt(publicKey.getEncoded())),
            Algorithms.RSA, privateKey, publicKey);
    }

    @Override
    protected byte[] encrypt(CipherHolder holder, byte[] data) {
        return IBase64.encrypt(doCipher(holder.getEncrypt(), holder.getPublicKey(), data));
    }

    @Override
    protected byte[] decrypt(CipherHolder holder, byte[] data) {
        return doCipher(holder.getDecrypt(), holder.getPrivateKey(), IBase64.decrypt(data));
    }

    /**
     * 加/解密数据
     *
     * @param cipher cipher
     * @param key    key
     * @param datas  要加/解密的数据（BASE64 encode过的数据）
     * @return 加/解密结果
     */
    private byte[] doCipher(Cipher cipher, Key key, byte[] datas) {
        log.debug("开始非对称的加解密");
        RSAKey rsaKey = (RSAKey) key;

        //计算block大小
        int maxBlock = rsaKey.getModulus().bitLength() / 8;
        //加密时需要减11
        if (key instanceof RSAPublicKey) {
            maxBlock = maxBlock - 11;
        }

        log.debug("当前block大小为：[{}]，开始加/解密", maxBlock);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            for (int i = 0, offset = 0; datas.length > offset; i++, offset = maxBlock * i) {
                if (datas.length - offset > maxBlock) {
                    out.write(cipher.doFinal(datas, offset, maxBlock));
                } else {
                    out.write(cipher.doFinal(datas, offset, datas.length - offset));
                }
            }

            byte[] result = out.toByteArray();
            IOUtils.closeQuietly(out);
            return result;
        } catch (IOException | IllegalBlockSizeException | BadPaddingException e) {
            log.warn("RSA加/解密失败", e);
            throw new SecureException(e);
        }
    }
}
