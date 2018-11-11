package com.joe.utils.proxy;

import java.lang.reflect.Type;
import java.util.Collections;

import org.junit.Assert;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月08日 10:49 JoeKerouac Exp $
 */
public class ProxyClientTest {

    private ProxyClient    client;
    private MethodMetadata metadata;

    public ProxyClientTest(ProxyClient client) {
        this.client = client;
        metadata = new MethodMetadata("say", String.class, new Type[] { String.class });
    }

    /**
     * 测试
     */
    public void doTest() {
        doCreate(true);
        doCreate(false);
        doBuilder(true);
        doBuilder(false);
        doFilter(true);
        doFilter(false);
    }

    /**
     * 
     * @param flag true表示使用Say，false表示使用SayDefault
     */
    private void doCreate(boolean flag) {
        Environment environment = new Environment(flag);
        Say sayHi = client.create(environment.clazz,
            Collections.singletonMap(metadata, environment.hiMethodProxy));

        Say sayHello = client.create(environment.clazz,
            Collections.singletonMap(metadata, environment.helloMethodProxy));

        doTest(sayHi, sayHello);
    }

    private void doBuilder(boolean flag) {
        Environment environment = new Environment(flag);
        Say sayHi = client.createBuilder(environment.clazz)
            .proxyMethod(metadata, environment.hiMethodProxy).build();
        Say sayHello = client.createBuilder(environment.clazz)
            .proxyMethod(metadata, environment.helloMethodProxy).build();
        doTest(sayHi, sayHello);
    }

    private void doFilter(boolean flag) {
        Environment environment = new Environment(flag);
        Say sayHi = client.create(environment.clazz, method -> {
            if (method.getName().equals("say")) {
                return environment.hiMethodProxy;
            } else {
                return null;
            }
        });

        Say sayHello = client.create(environment.clazz, method -> {
            if (method.getName().equals("say")) {
                return environment.helloMethodProxy;
            } else {
                return null;
            }
        });

        doTest(sayHi, sayHello);
    }

    /**
     * 测试
     * @param sayHi hi代理
     * @param sayHello hello代理
     */
    private void doTest(Say sayHi, Say sayHello) {
        Assert.assertEquals(sayHi.say("123"), "hi:123");
        Assert.assertEquals(sayHello.say("123"), "hello:123");
        Assert.assertSame(sayHi.getClass().getClassLoader(), sayHello.getClass().getClassLoader());
    }

    public interface Say {
        String say(String str);
    }

    public static class SayDefault implements Say {
        public String say(String str) {
            return "default";
        }
    }

    class Hello {
        String hi(String str) {
            return "hi:" + str;
        }

        String hello(String str) {
            return "hello:" + str;
        }
    }

    private class Environment {
        Interception         hiMethodProxy;
        Interception         helloMethodProxy;
        Class<? extends Say> clazz;

        Environment(boolean flag) {
            clazz = flag ? Say.class : SayDefault.class;

            hiMethodProxy = (params, callable, method) -> {
                try {
                    if (flag) {
                        Assert.assertNull(callable);
                    } else {
                        Assert.assertEquals(callable.call(), "default");
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return new Hello().hi((String) params[0]);
            };
            helloMethodProxy = (params, callable, method) -> {
                try {
                    if (flag) {
                        Assert.assertNull(callable);
                    } else {
                        Assert.assertEquals(callable.call(), "default");
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return new Hello().hello((String) params[0]);
            };
        }
    }
}
