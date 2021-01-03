package com.joe.utils.secure.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.joe.utils.codec.Hex;
import com.joe.utils.pool.ObjectPoolImpl;
import com.joe.utils.pool.PooledObject;
import com.joe.utils.secure.MessageDigestUtil;
import com.joe.utils.secure.exception.SecureException;

/**
 * 消息摘要工具类
 *
 * @author joe
 * @version 2018.07.11 16:57
 */
public class MessageDigestUtilImpl implements MessageDigestUtil {
    private static final Map<String, ObjectPoolImpl<MessageDigest>> CACHE = new ConcurrentHashMap<>();
    private Algorithms algorithms;

    private MessageDigestUtilImpl(Algorithms algorithms) {
        this.algorithms = algorithms;

        CACHE.computeIfAbsent(algorithms.name(), key -> {
            ObjectPoolImpl<MessageDigest> pool = new ObjectPoolImpl<>(() -> getMessageDigest(algorithms));
            // 快速验证
            pool.get().close();
            return pool;
        });
    }

    /**
     * 获取摘要算法实例
     *
     * @param algorithms
     *            算法
     * @return 摘要算法实例
     */
    public static MessageDigestUtil buildInstance(Algorithms algorithms) {
        return new MessageDigestUtilImpl(algorithms);
    }

    /**
     * 获取指定算法对应的MessageDigest
     *
     * @param algorithms
     *            算法
     * @return MessageDigest
     */
    private MessageDigest getMessageDigest(Algorithms algorithms) {
        try {
            return MessageDigest.getInstance(algorithms.getAlgorithms());
        } catch (NoSuchAlgorithmException exception) {
            throw new SecureException("当前系统没有指定的算法提供者:[" + "" + "]", exception);
        }
    }

    /**
     * 获取数据摘要
     *
     * @param data
     *            数据
     * @return 对应的摘要（转为了16进制字符串）
     */
    public String digest(String data) {
        return new String(Hex.encodeHex(digest(data.getBytes()), true));
    }

    /**
     * 获取数据摘要
     *
     * @param data
     *            数据
     * @return 对应的摘要
     */
    public byte[] digest(byte[] data) {
        try (PooledObject<MessageDigest> holder = CACHE.get(algorithms.name()).get()) {
            return holder.get().digest(data);
        }
    }
}
