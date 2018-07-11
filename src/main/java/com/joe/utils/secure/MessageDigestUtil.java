package com.joe.utils.secure;


import com.joe.utils.pool.ObjectPool;
import com.joe.utils.codec.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息摘要工具类
 *
 * @author joe
 * @version 2018.07.11 16:57
 */
public class MessageDigestUtil {
    private static final Map<Algorithms, ObjectPool<MessageDigest>> CACHE = new HashMap<>();
    private Algorithms algorithms;

    public MessageDigestUtil(Algorithms algorithms) {
        this.algorithms = algorithms;
        ObjectPool<MessageDigest> privatePool = CACHE.get(algorithms);
        if (privatePool == null) {
            synchronized (algorithms) {
                privatePool = CACHE.get(algorithms);
                if (privatePool == null) {
                    privatePool = new ObjectPool<>(() -> getMessageDigest(algorithms));
                    CACHE.put(algorithms, privatePool);
                }
            }
        }
    }

    /**
     * 获取指定算法对应的MessageDigest
     *
     * @param algorithms 算法
     * @return MessageDigest
     */
    private MessageDigest getMessageDigest(Algorithms algorithms) {
        try {
            return MessageDigest.getInstance(algorithms.algorithms);
        } catch (NoSuchAlgorithmException exception) {
            throw new SecureException("当前系统没有指定的算法提供者:[" + "" + "]", exception);
        }
    }

    /**
     * 获取数据摘要
     *
     * @param data 数据
     * @return 对应的摘要（转为了16进制字符串）
     */
    public String digest(String data) {
        return new String(Hex.encodeHex(digest(data.getBytes()), true));
    }

    /**
     * 获取数据摘要
     *
     * @param data 数据
     * @return 对应的摘要
     */
    public byte[] digest(byte[] data) {
        try (ObjectPool.PoolObjectHolder<MessageDigest> holder = CACHE.get(algorithms).get()) {
            byte[] result = holder.get().digest(data);
            return result;
        }
    }

    /**
     * 算法
     */
    public enum Algorithms {
        MD2("MD2"), MD5("MD5"), SHA1("SHA-1"), SHA224("SHA-224"), SHA256("SHA-256"), SHA384("SHA-384"), SHA512
                ("SHA-512");
        private String algorithms;

        Algorithms(String algorithms) {
            this.algorithms = algorithms;
        }
    }
}
