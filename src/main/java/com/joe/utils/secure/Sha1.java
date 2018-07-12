package com.joe.utils.secure;

import org.apache.commons.codec.digest.DigestUtils;

public class Sha1 {

    public String encrypt(String content) {
        return DigestUtils.sha1Hex(content);
    }

    public byte[] encrypt(byte[] content) {
        return DigestUtils.sha1Hex(content).getBytes();
    }

}
