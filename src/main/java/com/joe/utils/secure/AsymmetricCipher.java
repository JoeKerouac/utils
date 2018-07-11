package com.joe.utils.secure;

import com.joe.utils.common.IOUtils;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPublicKey;

/**
 * 非对称加密（RSA）
 *
 * @author joe
 * @version 2018.07.11 21:17
 */
@Slf4j
public class AsymmetricCipher extends AbstractCipher {

    /**
     * 非对称加密构造器
     *
     * @param privateKey PKCS8格式的私钥
     * @param publicKey  X509格式的公钥
     */
    public AsymmetricCipher(String privateKey, String publicKey) {
        super(Algorithms.RSA, privateKey, publicKey);
    }

    @Override
    protected byte[] encrypt(CipherHolder holder, byte[] data) {
        return BASE_64.encrypt(doCipher(holder.getEncrypt() , holder.getPublicKey() , data));
    }

    @Override
    protected byte[] decrypt(CipherHolder holder, byte[] data) {
        return doCipher(holder.getDecrypt() , holder.getPrivateKey() , BASE_64.decrypt(data));
    }


    @Override
    protected Key buildPrivateKey(Algorithms algorithm, String privateKey, int keySize) {
        return KeyTools.getPrivateKeyFromPKCS8(algorithm.toString(), new ByteArrayInputStream(privateKey.getBytes()));
    }

    @Override
    protected Key buildPublicKey(Algorithms algorithm, String publicKey, int keySize) {
        return KeyTools.getPublicKeyFromX509(algorithm.toString(), new ByteArrayInputStream(publicKey.getBytes()));
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
