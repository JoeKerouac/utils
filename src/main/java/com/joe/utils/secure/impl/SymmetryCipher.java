package com.joe.utils.secure.impl;

import com.joe.utils.secure.CipherUtil;
import com.joe.utils.secure.KeyTools;
import com.joe.utils.secure.exception.SecureException;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import java.util.HashMap;
import java.util.Map;

/**
 * 对称加密工具
 *
 * @author joe
 * @version 2018.07.11 18:47
 */
@Slf4j
public class SymmetryCipher extends AbstractCipher {
    private static final Map<String, SecretKey> KEY_CACHE = new HashMap<>();

    private SymmetryCipher(Algorithms algorithms, SecretKey key) {
        super(new String(key.getEncoded()), algorithms, key, key);
    }

    /**
     * 生成AES加密器
     *
     * @param seed 随机数种子
     * @return SymmetryCipher
     */
    public static CipherUtil buildInstance(String seed) {
        return buildInstance(Algorithms.AES, seed, 128);
    }

    /**
     * SymmetryCipher构造器，采用默认keySize
     *
     * @param algorithms 算法，当前仅支持AES和DES
     * @param seed       随机数种子
     * @return SymmetryCipher
     */
    public static CipherUtil buildInstance(Algorithms algorithms, String seed) {
        int keySize = 0;
        if (algorithms == Algorithms.AES) {
            keySize = 128;
        } else if (algorithms == Algorithms.DES) {
            keySize = 56;
        }
        return buildInstance(algorithms, seed, keySize);
    }

    /**
     * SymmetryCipher构造器
     *
     * @param algorithms 算法，当前仅支持AES和DES
     * @param seed       随机数种子
     * @param keySize    keySize
     * @return SymmetryCipher
     */
    public static CipherUtil buildInstance(Algorithms algorithms, String seed, int keySize) {
        String id = (algorithms.name() + seed + keySize).intern();
        SecretKey key;
        if ((key = KEY_CACHE.get(id)) == null) {
            synchronized (id) {
                if ((key = KEY_CACHE.get(id)) == null) {
                    key = KeyTools.buildKey(algorithms, seed, keySize);
                    KEY_CACHE.put(id, key);
                }
            }
        }
        return buildInstance(algorithms, key.getEncoded());
    }


    /**
     * SymmetryCipher构造器
     *
     * @param algorithms 算法
     * @param keySpec    keySpec
     */
    public static CipherUtil buildInstance(Algorithms algorithms, byte[] keySpec) {
        SecretKey key = KeyTools.buildKey(algorithms, keySpec);
        return new SymmetryCipher(algorithms, key);
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
