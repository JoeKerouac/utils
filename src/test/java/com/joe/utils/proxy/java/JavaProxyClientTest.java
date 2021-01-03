package com.joe.utils.proxy.java;

import org.junit.Assert;
import org.junit.Test;

import com.joe.utils.proxy.Interception;
import com.joe.utils.proxy.ProxyClient;
import com.joe.utils.proxy.ProxyClientTestHelper;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年12月05日 23:27 JoeKerouac Exp $
 */
public class JavaProxyClientTest {
    @Test
    public void doTest() {
        ProxyClient client = ProxyClient.getInstance(ProxyClient.ClientType.JAVA);

        Interception interception = (target, params, method, invoker) -> {
            if (method.getName().equals("say")) {
                return new Hello().hi((String)params[0]);
            } else {
                return invoker.call();
            }
        };

        Say say = client.create(Say.class, interception);

        String str = "123";
        Assert.assertEquals(say.say(str), "hi:" + str);

        ProxyClientTestHelper.doObjectMethodTest(client);
        ProxyClientTestHelper.doProxyParentMethodTest(client);
        ProxyClientTestHelper.doMultiProxy(client);
    }

    public interface Say {
        String say(String str);
    }

    class Hello {
        String hi(String str) {
            return "hi:" + str;
        }
    }
}
