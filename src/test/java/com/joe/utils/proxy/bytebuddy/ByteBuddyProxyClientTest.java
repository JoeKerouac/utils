package com.joe.utils.proxy.bytebuddy;

import org.junit.Test;

import com.joe.utils.proxy.ProxyClient;
import com.joe.utils.proxy.ProxyClientTestHelper;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月11日 12:27 JoeKerouac Exp $
 */
public class ByteBuddyProxyClientTest {

    @Test
    public void doTest() {
        new ProxyClientTestHelper(ProxyClient.getInstance(ProxyClient.ClientType.BYTE_BUDDY))
            .doTest();
    }
}
