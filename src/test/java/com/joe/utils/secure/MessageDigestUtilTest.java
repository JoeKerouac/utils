package com.joe.utils.secure;

import com.joe.utils.common.Assert;
import org.junit.Test;

/**
 * 测试摘要工具类
 *
 * @author joe
 * @version 2018.07.11 18:05
 */
public class MessageDigestUtilTest {
    private MessageDigestUtil util;
    private String[] algorithmsList = {"MD2", "MD5", "SHA1", "SHA224", "SHA256", "SHA384", "SHA512"};

    @Test
    public void doDigest() {
        for (String algorithms : algorithmsList) {
            util = new MessageDigestUtil(MessageDigestUtil.Algorithms.valueOf(algorithms));
            Assert.notNull(util.digest("你好啊"));
        }
    }
}
