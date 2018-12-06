package com.joe.utils.proxy.cglib;

import com.joe.utils.proxy.ProxyClient;
import com.joe.utils.proxy.ProxyClientTestHelper;
import org.junit.Test;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月11日 12:30 JoeKerouac Exp $
 */
public class CglibProxyClientTest {

    @Test
    public void doTest() {
        new ProxyClientTestHelper(ProxyClient.getInstance(ProxyClient.ClientType.CGLIB)).doTest();
    }
}
