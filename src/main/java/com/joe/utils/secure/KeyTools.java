package com.joe.utils.secure;

import com.joe.utils.codec.IBase64;
import com.joe.utils.common.IOUtils;
import com.joe.utils.common.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Key工具
 *
 * @author joe
 * @version 2018.07.11 21:19
 */
public class KeyTools {
    private static final IBase64 BASE_64 = new IBase64();

    /**
     * 从PKCS8格式的文件中获取私钥
     *
     * @param algorithm 加密算法名称
     * @param ins       PKCS8文件的输入流
     * @return 私钥
     */
    public static PrivateKey getPrivateKeyFromPKCS8(String algorithm, InputStream ins) {
        if (ins == null || StringUtils.isEmpty(algorithm)) {
            return null;
        }
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            byte[] encodedKey = IOUtils.read(ins);
            encodedKey = BASE_64.decrypt(encodedKey);
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            throw new SecureException("构建[" + algorithm + "]私钥失败", e);
        }
    }

    /**
     * 从X509格式的文件中获取public key
     *
     * @param algorithm 加密算法名称
     * @param ins       X509格式的public key文件的输入流
     * @return 公钥
     */
    public static PublicKey getPublicKeyFromX509(String algorithm, InputStream ins) {
        if (ins == null || StringUtils.isEmpty(algorithm)) {
            return null;
        }

        try {
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            byte[] encodedKey = IOUtils.read(ins);
            encodedKey = BASE_64.decrypt(encodedKey);
            return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            throw new SecureException("构建[" + algorithm + "]私钥失败", e);
        }
    }
}
