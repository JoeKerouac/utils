package com.joe.utils.protocol;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author JoeKerouac
 * @version 2019年11月19日 11:17
 */
public class DatagramTest {

    @Test
    public void doTest() {
        String str = "你好";
        byte[] data = str.getBytes();
        // 先构建为数据报
        Datagram datagram = DatagramUtil.build(data, DatagramConst.Type.FILE, DatagramConst.Version.V1);
        // 获取数据反解析
        datagram = DatagramUtil.decode(datagram.getData());
        // 判断数据是否一致
        Assert.assertEquals(str, new String(datagram.getBody()));
    }
}
