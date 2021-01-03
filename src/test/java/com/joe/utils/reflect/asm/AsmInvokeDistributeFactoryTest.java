package com.joe.utils.reflect.asm;

import org.junit.Assert;
import org.junit.Test;

import com.joe.utils.reflect.ByteCodeUtils;
import com.joe.utils.reflect.InvokeDistribute;
import com.joe.utils.reflect.InvokeDistributeFactory;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年02月21日 20:46 JoeKerouac Exp $
 */
public class AsmInvokeDistributeFactoryTest {
    private String name = "joe";
    private String owner = ByteCodeUtils.convert(User.class);
    private String sayMethodOwner = ByteCodeUtils.convert(AbstractUser.class);
    private String helloMethodOwner = ByteCodeUtils.convert(Hello.class);
    private String hiMethodOwner = ByteCodeUtils.convert(Hi.class);
    private String setMethod = "setName";
    private String getMethod = "getName";
    private String sayMethod = "say";
    private String sayHiMethod = "sayHi";
    private String sayHelloMethod = "sayHello";
    private String talkMethod = "talk";
    private String setMethodDesc = ByteCodeUtils.getDesc(void.class, String.class);
    private String desc = ByteCodeUtils.getDesc(String.class);
    private InvokeDistributeFactory factory = new AsmInvokeDistributeFactory();

    /**
     * 测试一般正确场景
     * 
     * @throws NoSuchMethodException
     *             NoSuchMethodException
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testInvoke() throws NoSuchMethodException {
        User user = new User();
        InvokeDistribute invokeDistribute = factory.build(user);

        // 调用setName方法
        invokeDistribute.invoke(owner, setMethod, setMethodDesc, name);

        // 验证setName方法调用成功
        Assert.assertEquals(name, user.getName());

        // 验证动态调用getName
        Assert.assertEquals(name, invokeDistribute.invoke(owner, getMethod, desc));
    }

    /**
     * 测试错误场景
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testNotFound() {
        User user = new User();
        InvokeDistribute invokeDistribute = factory.build(user);

        // 设置错误的owner
        NoSuchMethodException noSuchMethodException = null;
        try {
            invokeDistribute.invoke(owner + "1", setMethod, setMethodDesc, name);
        } catch (NoSuchMethodException e) {
            noSuchMethodException = e;
        }
        Assert.assertNotNull(noSuchMethodException);

        // 设置错误的方法名
        noSuchMethodException = null;
        try {
            invokeDistribute.invoke(owner, setMethod + 1, setMethodDesc, name);
        } catch (NoSuchMethodException e) {
            noSuchMethodException = e;
        }
        Assert.assertNotNull(noSuchMethodException);

        // 设置错误的owner，调用say方法，此时抽象类AbstractUser中含有say方法，传入错误的owner，Hello，虽然Hello接口中也声明了say方法
        // 但是调用会出错，保证行为与直接调用一致（直接调用say方法会调用AbstractUser中的say方法而不是Hello中的default say方法）
        noSuchMethodException = null;
        try {
            invokeDistribute.invoke(helloMethodOwner, sayMethod, desc, name);
        } catch (NoSuchMethodException e) {
            noSuchMethodException = e;
        }
        Assert.assertNotNull(noSuchMethodException);
    }

    /**
     * 测试动态调用方法的行为与直接调用是否一致（主要测试当代理的类存在父类，并且父类也声明同一个方法的时候的行为是否一致）
     * 
     * @throws NoSuchMethodException
     *             NoSuchMethodException
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testInvokeAction() throws NoSuchMethodException {
        User user = new User();
        InvokeDistribute invokeDistribute = factory.build(user);

        Assert.assertEquals(user.talk(), invokeDistribute.invoke(owner, talkMethod, desc));
        Assert.assertEquals(user.say(), invokeDistribute.invoke(sayMethodOwner, sayMethod, desc));
        Assert.assertEquals(user.sayHello(), invokeDistribute.invoke(helloMethodOwner, sayHelloMethod, desc));
        Assert.assertEquals(user.sayHi(), invokeDistribute.invoke(hiMethodOwner, sayHiMethod, desc));
    }

    @Test
    public void test() {}

    public static class User extends AbstractUser implements Hi {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String talk() {
            return "talk by User";
        }
    }

    public static abstract class AbstractUser {
        public String say() {
            return "absUser";
        }

        public String talk() {
            return "talk";
        }
    }

    public interface Hi extends Hello {
        default String say() {
            return "say";
        }

        default String sayHi() {
            return "sayHi";
        }
    }

    public interface Hello {
        default String say() {
            return "say hello";
        }

        default String sayHello() {
            return "sayHello";
        }
    }
}
