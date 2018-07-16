package com.joe.utils.secure.impl;

import org.junit.Test;

import com.joe.utils.common.Assert;
import com.joe.utils.secure.MessageDigestUtil;

/**
 * 测试摘要工具类
 *
 * @author joe
 * @version 2018.07.11 18:05
 */
public class MessageDigestUtilTest {
    private MessageDigestUtil util;
    private String[]          algorithmsList = { "MD2", "MD5", "SHA1", "SHA224", "SHA256", "SHA384",
                                                 "SHA512" };

    @Test
    public void doDigest() {
        for (String algorithms : algorithmsList) {
            util = MessageDigestUtilImpl
                .buildInstance(MessageDigestUtilImpl.Algorithms.valueOf(algorithms));
            Assert.notNull(util.digest("你好啊"));
        }
    }
}
