package com.joe.utils.secure;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

public class IBase64 implements Encipher {
	/*
	 * 编码器
	 */
	private static final Encoder encoder = Base64.getEncoder();
	/*
	 * 解码器
	 */
	private static final Decoder decoder = Base64.getDecoder();

	/**
	 * base64编码
	 * 
	 * @param input
	 *            要编码的数据
	 * @return 编码后的数据
	 */
	public byte[] encrypt(byte[] input) {
		return encoder.encode(input);
	}

	/**
	 * base64编码
	 * 
	 * @param input
	 *            要编码的数据
	 * @return 编码后的数据
	 */
	public String encrypt(String input) {
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
	public byte[] decrypt(byte[] input) {
		return decoder.decode(input);
	}

	/**
	 * base64解码
	 * 
	 * @param input
	 *            编码后的数据
	 * @return 解码后（编码前）的数据
	 */
	public String decrypt(String input) {
		byte[] result = decrypt(input.getBytes());
		return new String(result);
	}
}