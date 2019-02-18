package com.joe.utils.proxy;

import org.junit.Assert;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年11月08日 10:49 JoeKerouac Exp $
 */
public class ProxyClientTestHelper {
    private ProxyClient client;

    public ProxyClientTestHelper(ProxyClient client) {
        this.client = client;
    }

    /**
     * 测试入口
     */
    public void doTest() {
        doCreate(true);
        doCreate(false);
        doObjectMethodTest(client);
        doProxyParentMethodTest(client);

    }

    /**
     * 测试
     * @param client
     */
    public static void doProxyParentMethodTest(ProxyClient client) {
        Interception interception = (target, params, invoker,
                                     method) -> method.getName().equals("say") ? null
                                         : invoker.call();
        Say say = client.create(Say.class, interception);
        Assert.assertTrue(say instanceof ProxyParent);
        ProxyParent parent = (ProxyParent) say;
        Assert.assertNull(parent.getTarget());
        Assert.assertEquals(parent.getInterfaces().length, 1);
        Assert.assertEquals(parent.getTargetClass(), Say.class);
    }

    /**
     * 测试Object方法行为（只对toString、hashCode、equals方法的行为测试，主要是防止递归）
     */
    public static void doObjectMethodTest(ProxyClient client) {
        Interception interception = (target, params, invoker,
                                     method) -> method.getName().equals("say") ? null
                                         : invoker.call();
        Say say1 = client.create(Say.class, interception);

        Say say2 = client.create(Say.class, interception);

        Assert.assertNotEquals(say1, say2);
        Assert.assertNotEquals(say1.toString(), say2.toString());
        Assert.assertNotEquals(say1.hashCode(), say2.hashCode());

        // 不保证不同对象getClass返回的值相同
        Assert.assertNotNull(say1.getClass());
    }

    /**
     * 测试{@link ProxyClient#create(Class, Interception)}
     * 
     * @param flag true表示使用Say，false表示使用SayDefault
     */
    private void doCreate(boolean flag) {
        Environment environment = new Environment(flag);
        Say sayHi = client.create(environment.clazz, environment.hiMethodProxy);

        Say sayHello = client.create(environment.clazz, environment.helloMethodProxy);

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

            hiMethodProxy = (target, params, callable, method) -> {
                if (method.getName().equals("say")) {
                    if (flag) {
                        Assert.assertNull(callable);
                    } else {
                        Assert.assertEquals(callable.call(), "default");
                    }
                    return new Hello().hi((String) params[0]);
                } else {
                    return callable.call();
                }

            };
            helloMethodProxy = (target, params, callable, method) -> {
                if (method.getName().equals("say")) {
                    if (flag) {
                        Assert.assertNull(callable);
                    } else {
                        Assert.assertEquals(callable.call(), "default");
                    }
                    return new Hello().hello((String) params[0]);
                } else {
                    return callable.call();
                }

            };
        }
    }
}
