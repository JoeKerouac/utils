package com.joe.utils.secure;

/**
 * 签名工具
 *
 * @author joe
 * @version 2018.07.12 13:46
 */
public interface SignatureUtil {
    /**
     * 使用私钥签名
     *
     * @param content 要签名的数据
     * @return 签名结果（会对签名结果做BASE64 encode处理）
     */
    String sign(String content);

    /**
     * 使用私钥签名
     *
     * @param content 要签名的数据
     * @return 签名结果（会对签名结果做BASE64 encode处理）
     */
    byte[] sign(byte[] content);

    /**
     * 使用公钥校验签名
     *
     * @param content 原文
     * @param data    签名数据
     * @return 返回true表示校验成功
     */
    boolean checkSign(String content, String data);

    /**
     * 使用公钥校验签名
     *
     * @param content 原文
     * @param data    签名数据（BASE64 encode过的）
     * @return 返回true表示校验成功
     */
    boolean checkSign(byte[] content, byte[] data);

    /**
     * 签名算法
     */
    enum Algorithms {
                     SHA1withRSA, SHA224withRSA, SHA256withRSA, SHA384withRSA, SHA512withRSA,
    }
}
