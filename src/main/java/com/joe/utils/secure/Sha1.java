package com.joe.utils.secure;

import org.apache.commons.codec.digest.DigestUtils;

public class Sha1 implements CipherUtil {

	@Override
	public String encrypt(String content) {
		return DigestUtils.sha1Hex(content);
	}

	@Override
	public byte[] encrypt(byte[] content) {
		return DigestUtils.sha1Hex(content).getBytes();
	}

	@Override
	public String decrypt(String content) {
		return null;
	}

	@Override
	public byte[] decrypt(byte[] content) {
		return null;
	}

}
