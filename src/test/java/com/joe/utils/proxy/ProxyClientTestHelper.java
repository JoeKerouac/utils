package com.joe.utils.proxy;

import java.lang.reflect.Type;
import java.util.Collections;

import org.junit.Assert;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月08日 10:49 JoeKerouac Exp $
 */
public class ProxyClientTestHelper {

    private static final MethodMetadata METADATA = new MethodMetadata("say", String.class,
        new Type[] { String.class });

    private ProxyClient                 client;

    public ProxyClientTestHelper(ProxyClient client) {
        this.client = client;
    }

    /**
     * 测试入口
     */
    public void doTest() {
        doCreate(true);
        doCreate(false);
        doBuilder(true);
        doBuilder(false);
        doFilter(true);
        doFilter(false);
        doObjectMethodTest(client);
    }

    /**
     * 测试Object方法行为（只对toString、hashCode、equals方法的行为测试，主要是防止递归）
     */
    public static void doObjectMethodTest(ProxyClient client) {
        Say say1 = client.create(Say.class,
            Collections.singletonMap(METADATA, (params, callable, method) -> null));

        Say say2 = client.create(Say.class,
            Collections.singletonMap(METADATA, (params, callable, method) -> null));

        Assert.assertNotEquals(say1, say2);
        Assert.assertNotEquals(say1.toString(), say2.toString());
        Assert.assertNotEquals(say1.hashCode(), say2.hashCode());

        // 不保证不同对象getClass返回的值相同
        Assert.assertNotNull(say1.getClass());
    }

    /**
     * 测试create方法直接生成代理
     * 
     * @param flag true表示使用Say，false表示使用SayDefault
     */
    private void doCreate(boolean flag) {
        Environment environment = new Environment(flag);
        Say sayHi = client.create(environment.clazz,
            Collections.singletonMap(METADATA, environment.hiMethodProxy));

        Say sayHello = client.create(environment.clazz,
            Collections.singletonMap(METADATA, environment.helloMethodProxy));

        doTest(sayHi, sayHello);
    }

    /**
     * 测试builder方法生成代理
     * @param flag true表示使用Say，false表示使用SayDefault
     */
    private void doBuilder(boolean flag) {
        Environment environment = new Environment(flag);
        Say sayHi = client.createBuilder(environment.clazz)
            .proxyMethod(METADATA, environment.hiMethodProxy).build();
        Say sayHello = client.createBuilder(environment.clazz)
            .proxyMethod(METADATA, environment.helloMethodProxy).build();
        doTest(sayHi, sayHello);
    }

    /**
     * 测试filter
     * @param flag true表示使用Say，false表示使用SayDefault
     */
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

    static class Hello {
        String hi(String str) {
            return "hi:" + str;
        }

        String hello(String str) {
            return "hello:" + str;
        }
    }

    private static class Environment {
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
