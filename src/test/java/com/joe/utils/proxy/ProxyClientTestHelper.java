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
        doMultiProxy(client);
    }

    /**
     * 测试
     * @param client
     */
    public static void doProxyParentMethodTest(ProxyClient client) {
        Interception interception = (target, params, method,
                                     invoker) -> method.getName().equals("say") ? null
                                         : invoker.call();
        Say say = client.create(Say.class, interception);
        Assert.assertTrue(say instanceof ProxyParent);
        ProxyParent parent = (ProxyParent) say;
        Assert.assertNull(parent.GET_TARGET());
        Assert.assertEquals(parent.GET_INTERFACES().length, 1);
        Assert.assertEquals(parent.GET_TARGET_CLASS(), Say.class);
        Assert.assertEquals(parent.GET_INTERCEPTION(), interception);
    }

    /**
     * 测试Object方法行为（只对toString、hashCode、equals方法的行为测试，主要是防止递归）
     */
    public static void doObjectMethodTest(ProxyClient client) {
        Interception interception = (target, params, method,
                                     invoker) -> method.getName().equals("say") ? null
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
     * 测试多重代理
     * @param client 代理客户端
     */
    public static void doMultiProxy(ProxyClient client) {
        String testContent = "testContent";

        Convert convert1 = new Convert() {
            @Override
            public String convert1(String content) {
                return "1:convert1:" + content;
            }

            @Override
            public String convert2(String content) {
                return "1:convert2:" + content;
            }
        };

        // 对convert1进行代理
        Convert convert2 = client.create(Convert.class, convert1,
            (target, params, method, invoker) -> {
                if (method.getName().equals("convert1")) {
                    // 校验父调用和代理对象一致
                    Assert.assertEquals(invoker.call(),
                        ((Convert) target).convert1((String) params[0]));
                    return invoker.call();
                } else if (method.getName().equals("convert2")) {
                    // 对此方法进行代理
                    return "2:convert2:" + params[0];
                } else {
                    return invoker.call();
                }
            });

        // 对convert2进行代理
        Convert convert3 = client.create(Convert.class, convert2,
            (target, params, method, invoker) -> {
                if (method.getName().equals("convert1")) {
                    // 校验父调用和代理对象一致
                    Assert.assertEquals(invoker.call(),
                        ((Convert) target).convert1((String) params[0]));
                    return invoker.call();
                } else if (method.getName().equals("convert2")) {
                    // 对此方法进行代理
                    return "3:convert2:" + params[0];
                } else {
                    return invoker.call();
                }
            });

        Convert convert4 = client.create(Convert.class, convert3,
            (target, params, method, invoker) -> {
                if (method.getName().equals("convert1")) {
                    // 校验父调用和代理对象一致
                    Assert.assertEquals(invoker.call(),
                        ((Convert) target).convert1((String) params[0]));
                    return invoker.call();
                } else if (method.getName().equals("convert2")) {
                    // 对此方法进行代理
                    return "4:convert2:" + params[0];
                } else {
                    return invoker.call();
                }
            });

        // 因为convert1方法没有代理，所以应该与最顶层的父对象一致
        Assert.assertEquals(convert4.convert1(testContent), convert1.convert1(testContent));

        // convert2进行代理，应该与指定行为一致
        Assert.assertEquals(convert4.convert2(testContent), "4:convert2:" + testContent);
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

    public interface Convert {

        String convert1(String content);

        String convert2(String content);
    }

    private static class Environment {
        Interception         hiMethodProxy;
        Interception         helloMethodProxy;
        Class<? extends Say> clazz;

        Environment(boolean flag) {
            clazz = flag ? Say.class : SayDefault.class;

            hiMethodProxy = (target, params, method, callable) -> {
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
            helloMethodProxy = (target, params, method, callable) -> {
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
