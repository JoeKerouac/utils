package com.joe.utils.codec;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

/**
 * BASE64编码
 *
 * @author joe
 * @version 2018.07.20 18:18
 */
public class IBase64 {
    /**
     * 编码器
     */
    private static final Encoder ENCODER = Base64.getEncoder();
    /**
     * 解码器
     */
    private static final Decoder DECODER = Base64.getDecoder();

    /**
     * base64编码
     *
     * @param input
     *            要编码的数据
     * @return 编码后的数据
     */
    public static byte[] encrypt(byte[] input) {
        return ENCODER.encode(input);
    }

    /**
     * base64编码
     *
     * @param input
     *            要编码的数据
     * @return 编码后的数据
     */
    public static String encrypt(String input) {
        byte[] result = encrypt(input.getBytes());
        return new String(result);
    }

    /**
     * base64解码
     *
     * @param input
     *            编码后的数据
     * @return 解码后（编码前）的数据
     */
    public static byte[] decrypt(byte[] input) {
        return DECODER.decode(input);
    }

    /**
     * base64解码
     *
     * @param input
     *            编码后的数据
     * @return 解码后（编码前）的数据
     */
    public static String decrypt(String input) {
        byte[] result = decrypt(input.getBytes());
        return new String(result);
    }
}