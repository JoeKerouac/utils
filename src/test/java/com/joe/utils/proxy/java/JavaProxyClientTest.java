package com.joe.utils.proxy.java;

import java.lang.reflect.Type;
import java.util.Collections;

import com.joe.utils.proxy.ProxyClientTestHelper;
import org.junit.Assert;
import org.junit.Test;

import com.joe.utils.proxy.Interception;
import com.joe.utils.proxy.MethodMetadata;
import com.joe.utils.proxy.ProxyClient;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年12月05日 23:27 JoeKerouac Exp $
 */
public class JavaProxyClientTest {
    @Test
    public void doTest() {
        Interception hiMethodProxy = (params, callable, method) -> new Hello()
            .hi((String) params[0]);
        MethodMetadata metadata = new MethodMetadata("say", String.class,
            new Type[] { String.class });
        ProxyClient client = ProxyClient.getInstance(ProxyClient.ClientType.JAVA);

        Say say = client.create(Say.class, Collections.singletonMap(metadata, hiMethodProxy));

        String str = "123";
        Assert.assertEquals(say.say(str), "hi:" + str);

        ProxyClientTestHelper.doObjectMethodTest(client);
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
