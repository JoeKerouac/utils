package com.joe.utils.reflect.asm;

import org.junit.Assert;
import org.junit.Test;

import com.joe.utils.collection.CollectionUtil;

/**
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2019年02月21日 20:46 JoeKerouac Exp $
 */
public class AsmInvokeDistributeFactoryTest {
    private String name = "joe";

    @Test
    @SuppressWarnings("unchecked")
    public void test() {
        User user = new User();
        InvokeDistributeWraper<User> wraper = new InvokeDistributeWraper<>(user);
        wraper.invoke("setName", CollectionUtil.array(String.class), CollectionUtil.array(name));
        Assert.assertEquals(name, user.getName());

        Assert.assertEquals(name, wraper.invoke("getName", null, null));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNotFound() {
        User user = new User();
        InvokeDistributeWraper<User> wraper = new InvokeDistributeWraper<>(user);
        try {
            wraper.invoke("setNames", CollectionUtil.array(String.class),
                CollectionUtil.array(name));
            Assert.fail();
        } catch (Exception e) {
        }
    }

    public static class User {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
