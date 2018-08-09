# utils
常用工具集合

可以到maven中央仓库搜索JoeKerouac即可找到该项目

# 功能说明

### cluster包
> cluster包是一些集群工具，目前主要有一个redis实现的分布式集合、锁、PUB/SUB工具等。
#### ClusterManager
分布式资源管理器，包含获取一个分布式锁、获取分布式集合、获取分布式队列等功能，当前仅有redis的实现，使用方法如下（将redis host和port替换为你自己的可访问的redis）：
```java
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author joe
 * @version 2018.08.09 09:51
 */
public class ClusterManagerTest {
    String         pre  = ClusterManagerTest.class.getName() + "-%s";
    /**
     * redis host
     */
    String         host = "192.168.2.222";
    /**
     * redis port
     */
    int            port = 7001;
    String         text = "text";
    ClusterManager manager;
    ClusterManager check;

    @Test
    public void doLock() throws Exception {
        Lock lock = manager.getLock(String.format(pre, "Lock"));
        lock.lock();
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                Assert.assertFalse("分布式锁控制失败", lock.tryLock(10, TimeUnit.MILLISECONDS));
            } catch (Exception e) {
                Assert.assertNull(e.toString(), e);
            } finally {
                latch.countDown();
            }
        }).start();
        latch.await();
        lock.unlock();
    }

    @Test
    public void doMap() {
        Map<String, String> map = manager.getMap(String.format(pre, "Map"));
        map.put(text, text);
        Map<String, String> checkMap = check.getMap(String.format(pre, "Map"));
        Assert.assertTrue(checkMap != map);
        Assert.assertTrue(text.equals(checkMap.get(text)));
        map.clear();
    }

    @Test
    public void doBlockingDeque() throws Exception {
        BlockingDeque<String> deque = manager.getBlockingDeque(String.format(pre, "BlockingDeque"));
        BlockingDeque<String> checkDeque = manager
            .getBlockingDeque(String.format(pre, "BlockingDeque"));
        deque.put(text);

        CountDownLatch latch = new CountDownLatch(1);
        Thread thread = new Thread(() -> {
            try {
                Assert.assertEquals(checkDeque.take(), text);
                latch.countDown();
            } catch (InterruptedException e) {
                Assert.assertNull("从BlockingDeque获取数据失败", e);
            }
        });
        thread.setDaemon(true);
        thread.start();

        Assert.assertTrue(latch.await(3, TimeUnit.SECONDS));
    }

    @Test
    public void doPub() throws Exception {
        Topic<String> topic = manager.getTopic(String.format(pre, "PUB/SUB"));
        CountDownLatch latch = new CountDownLatch(1);
        topic.addListener(((channel, msg) -> {
            Assert.assertEquals(text, msg);
            latch.countDown();
        }));
        Topic<String> checkTopic = manager.getTopic(String.format(pre, "PUB/SUB"));
        checkTopic.publish(text);
        Assert.assertTrue(latch.await(3, TimeUnit.SECONDS));
    }

    @Before
    public void init() throws Exception {
        manager = ClusterManager.getInstance(host, port);
        check = ClusterManager.getInstance(host, port);
    }

    @After
    public void destroy() {
        manager.shutdown();
        check.shutdown();
    }
}
```

### codec包
> codec包主要提供一些转码工具，包含16进制转码和BASE64转码、解码
#### HEX/Base64
可以将byte数组转码为16进制字符数组，示例：
```java

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author joe
 * @version 2018.08.09 14:29
 */
public class CodecTest {
    byte[] datas;
    String hex;

    @Test
    public void doHex() {
        Assert.assertEquals(new String(Hex.encodeHex(datas, true)), hex);
    }

    @Test
    public void doBase64() {
        String text = "测试文本";
        Assert.assertEquals(IBase64.decrypt(IBase64.encrypt(text)), text);
        Assert.assertTrue(
            Arrays.equals(text.getBytes(), IBase64.decrypt(IBase64.encrypt(text.getBytes()))));
    }

    @Before
    public void init() {
        datas = new byte[] { 12, 56, 123, 1, 0, -45, 36, -123, -48, 94, 78, 53, 48, 12, 75, 48, 11,
                             10, 34, 56, 91, 61, 43, 81, 61 };
        hex = "0c387b0100d32485d05e4e35300c4b300b0a22385b3d2b513d";
    }
}
```
### secure包
> 提供常用加解密、摘要、签名验签等工具
#### CipherUtil
加解密工具，提供RSA、AES、DES三种算法的实现，子类有AsymmetricCipher（非对称加密）和SymmetryCipher（对称加密），使用示例：
```java
import org.junit.Before;
import org.junit.Test;

import com.joe.utils.secure.CipherUtil;
import com.joe.utils.secure.CipherUtilTest;

/**
 * @author joe
 * @version 2018.07.11 21:50
 */
public class AsymmetricCipherTest {
    private CipherUtil cipher;

    @Before
    public void init() {
        String privateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC93qGz/A0MKWZEVxrixXrt1PT/e/SKraLQOey7"
                            + "" + "4TXDOhenHrEs16kPkvSE0o3myMY6LqaaJ1PwPzKjtRng7XUd3UE4Axrarzzxz"
                            + "+c1Zas6QtEWiwDAZCcaLiBsh8adgdn8Fgcr3r8h"
                            + "/ZQyZzktkBawl8bixhLhC/zQYfUAIDayrT/NjTnnphgFuK8qtqxXHKcf/5yAEqVoSWIGtCPx26xPcgWvMpVWUrL7QJFdl05ln9dG"
                            + ""
                            + "1Lll9C/YCXav/Fpt2wSbxZue6Z5go7Uc5eFlB8Mal2VijNGj6ydA741gvlq8mRgvOCgvW3yoqAvhnJDh6raUscEjganZ5zmp0Gh7"
                            + ""
                            + "aqffAgMBAAECggEAH7EYdo1ctCn42vFbGHzz7ty75CURhVBEO9NfU2Dc83Av4II7+osouePCkqT+cIYUqEN"
                            + "/JX3pAdHapv6kiim4"
                            + "gbqblzjVc6kKWCZmpkAJG5lpgwTCpFpTOIh4ewUSvtmcw/n9SnJMnuTPprYaEiPZ1bIPzWxYXF3"
                            + "+3d1r4pB98Ma15a4+Nycj1XDZ"
                            + "ZDuNZnwpmK6kbQH6rDxx0PTyx2iPoYgYYL8kuhrcmGVOblZJsZwwhnSkZ0Rzdr0s0nNwPyBiH4BQP3D26ntABeXlznMVTartwxGD"
                            + ""
                            + "cvXUNWmGXeMTzptseb+/Qh64NNbg2d91FN3bcGMEx2s6+kSXvY5DWmIgyQKBgQD+N6PT/QnysAhoND+EZSiQXq7rA0cub"
                            + "+DhYfDS"
                            + "sIylzO8GijDe4WNpthsiT3gkRm7Qd/IECdGeFH6r7gM6sQ4/gTCNo/Wp5kCiNgpOh0J4wefN2isUSKYg7BT25"
                            + "+4kOjUNFoyN39XX"
                            + "wSWfR2YGs2RlcGSwc9KMGMbZTx2aYA42/QKBgQC/M3o9F9ywtNnAF5rOf/hYowbbGvZ8awfrTYYgQ"
                            + "+v8L+hC7AJnWborZwkM9dAo"
                            + "LovZOnaFnvPdQYC9PWRQhLxz2mEDaHjBfzo5gzA0PekmGg4sv0cSddkumf24WV+Vde9tPg"
                            + "/z2gq//cljS0To9Ez5QnrEMlcLF0f5"
                            + "JVE4AvDnCwKBgD9WDXMbcAcO5IlRuyF5MooFjP7waiOfrB97D0zuv5vvWv3+H/7nmKUVwdzif8RJ6AH4YEuxRpBj+stLGXLd+fbI"
                            + ""
                            + "cRhY6TPTMfc4D4lRGbaMWPxohqMfFoTNTMvXLPXJqr4ACDSwt85xSLZOS5UPzMuZHPz5XlOZOLGbjHyRwfjhAoGBAJ96CWBBsfK1"
                            + "" + "tdXyGk5ZEMSlngdM8wCNqXpLHC93V7LuJpUDE3IutgiTlUn5EdkiEcVrbQZAsXj"
                            + "/tKijNzOtsx4SrM5R6j7CpsbntGIurXP5UY9j"
                            + "L52nLDIhgS2tZftc9goxNM8ZM9Q1ky89zclcU4OoPou8zTtT/NVDmf3U/ihHAoGBAMTSE5be12gLjsz7RpabXJcoVG+wRo5nvn6P"
                            + ""
                            + "58dKwyQ/+GlboU3aMFHeBDkCZkC6ncc/yMsjgw+wWbNvzxhjDUBGvTNjNyJAQM0vstSJMJp+ciynfGic5k8ciiN"
                            + "/2Nd9qLENeKd4" + "oIRn0LXsBHcKYP/oLFpbFhyJ1a3+BH1Zo3ZL";

        String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvd6hs/wNDClmRFca4sV67dT0/3v0iq2i0Dnsu+E1wzoXp"
                           + ""
                           + "x6xLNepD5L0hNKN5sjGOi6mmidT8D8yo7UZ4O11Hd1BOAMa2q888c/nNWWrOkLRFosAwGQnGi4gbIfGnYHZ/BYHK96"
                           + "/If2UMmc5L"
                           + "ZAWsJfG4sYS4Qv80GH1ACA2sq0/zY0556YYBbivKrasVxynH/+cgBKlaEliBrQj8dusT3IFrzKVVlKy"
                           + "+0CRXZdOZZ/XRtS5ZfQv2"
                           + "Al2r/xabdsEm8WbnumeYKO1HOXhZQfDGpdlYozRo+snQO+NYL5avJkYLzgoL1t8qKgL4ZyQ4eq2lLHBI4Gp2ec5qdBoe2qn3wIDA"
                           + "" + "QAB";

        cipher = AsymmetricCipher.buildInstance(privateKey, publicKey);
    }

    @Test
    public void doCipher() {
        CipherUtilTest.checkCipher(cipher);
    }
}
```
```java

import org.junit.Before;
import org.junit.Test;

import com.joe.utils.secure.CipherUtil;
import com.joe.utils.secure.CipherUtilTest;

/**
 * @author joe
 * @version 2018.07.11 20:59
 */
public class SymmetryCipherTest {
    private CipherUtil aesCipher;
    private CipherUtil desCipher;

    @Before
    public void init() {
        desCipher = SymmetryCipher.buildInstance(AbstractCipher.Algorithms.DES,
            "123123123123123123", 56);
        aesCipher = SymmetryCipher.buildInstance(AbstractCipher.Algorithms.AES,
            "123123123123123123", 128);
    }

    @Test
    public void doAesCipher() {
        CipherUtilTest.checkCipher(aesCipher);
    }

    @Test
    public void doDesCipher() {
        CipherUtilTest.checkCipher(desCipher);
    }
}
```
```java
import org.junit.Assert;

/**
 * @author joe
 * @version 2018.07.11 21:55
 */
public class CipherUtilTest {
    private static String data = "这是测试加密字符串";

    /**
     * 检查加密是否能用
     *
     * @param cipher 加密器
     */
    public static void checkCipher(CipherUtil cipher) {
        Assert.assertTrue(data.equals(cipher.decrypt(cipher.encrypt(data))));
    }
}
```
#### MessageDigestUtil
签名工具，提供MD2、MD5、SHA-1、SHA-224、SHA-256、SHA-384、SHA-512算法，使用示例：
```java
import org.junit.Test;

import com.joe.utils.common.Assert;
import com.joe.utils.secure.MessageDigestUtil;

/**
 * 测试摘要工具类
 *
 * @author joe
 * @version 2018.07.11 18:05
 */
public class MessageDigestUtilTest {
    private MessageDigestUtil util;
    private String[]          algorithmsList = { "MD2", "MD5", "SHA1", "SHA224", "SHA256", "SHA384",
                                                 "SHA512" };

    @Test
    public void doDigest() {
        for (String algorithms : algorithmsList) {
            util = MessageDigestUtilImpl
                .buildInstance(MessageDigestUtilImpl.Algorithms.valueOf(algorithms));
            Assert.notNull(util.digest("你好啊"));
        }
    }
}
```
#### SignatureUtil
签名、验签工具接口，可以对数据签名，提供SHA1withRSA, SHA224withRSA, SHA256withRSA, SHA384withRSA, SHA512withRSA算法的实现，使用方法如下：
```java

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.joe.utils.secure.SignatureUtil;

/**
 * @author joe
 * @version 2018.07.12 14:21
 */
public class SignatureUtilImplTest {
    private SignatureUtil signatureUtil;
    private String        data;

    @Before
    public void init() {
        String privateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC93qGz/A0MKWZEVxrixXrt1PT/e/SKraLQOey7"
                            + "" + "4TXDOhenHrEs16kPkvSE0o3myMY6LqaaJ1PwPzKjtRng7XUd3UE4Axrarzzxz"
                            + "+c1Zas6QtEWiwDAZCcaLiBsh8adgdn8Fgcr3r8h"
                            + "/ZQyZzktkBawl8bixhLhC/zQYfUAIDayrT/NjTnnphgFuK8qtqxXHKcf/5yAEqVoSWIGtCPx26xPcgWvMpVWUrL7QJFdl05ln9dG"
                            + ""
                            + "1Lll9C/YCXav/Fpt2wSbxZue6Z5go7Uc5eFlB8Mal2VijNGj6ydA741gvlq8mRgvOCgvW3yoqAvhnJDh6raUscEjganZ5zmp0Gh7"
                            + ""
                            + "aqffAgMBAAECggEAH7EYdo1ctCn42vFbGHzz7ty75CURhVBEO9NfU2Dc83Av4II7+osouePCkqT+cIYUqEN"
                            + "/JX3pAdHapv6kiim4"
                            + "gbqblzjVc6kKWCZmpkAJG5lpgwTCpFpTOIh4ewUSvtmcw/n9SnJMnuTPprYaEiPZ1bIPzWxYXF3"
                            + "+3d1r4pB98Ma15a4+Nycj1XDZ"
                            + "ZDuNZnwpmK6kbQH6rDxx0PTyx2iPoYgYYL8kuhrcmGVOblZJsZwwhnSkZ0Rzdr0s0nNwPyBiH4BQP3D26ntABeXlznMVTartwxGD"
                            + ""
                            + "cvXUNWmGXeMTzptseb+/Qh64NNbg2d91FN3bcGMEx2s6+kSXvY5DWmIgyQKBgQD+N6PT/QnysAhoND+EZSiQXq7rA0cub"
                            + "+DhYfDS"
                            + "sIylzO8GijDe4WNpthsiT3gkRm7Qd/IECdGeFH6r7gM6sQ4/gTCNo/Wp5kCiNgpOh0J4wefN2isUSKYg7BT25"
                            + "+4kOjUNFoyN39XX"
                            + "wSWfR2YGs2RlcGSwc9KMGMbZTx2aYA42/QKBgQC/M3o9F9ywtNnAF5rOf/hYowbbGvZ8awfrTYYgQ"
                            + "+v8L+hC7AJnWborZwkM9dAo"
                            + "LovZOnaFnvPdQYC9PWRQhLxz2mEDaHjBfzo5gzA0PekmGg4sv0cSddkumf24WV+Vde9tPg"
                            + "/z2gq//cljS0To9Ez5QnrEMlcLF0f5"
                            + "JVE4AvDnCwKBgD9WDXMbcAcO5IlRuyF5MooFjP7waiOfrB97D0zuv5vvWv3+H/7nmKUVwdzif8RJ6AH4YEuxRpBj+stLGXLd+fbI"
                            + ""
                            + "cRhY6TPTMfc4D4lRGbaMWPxohqMfFoTNTMvXLPXJqr4ACDSwt85xSLZOS5UPzMuZHPz5XlOZOLGbjHyRwfjhAoGBAJ96CWBBsfK1"
                            + "" + "tdXyGk5ZEMSlngdM8wCNqXpLHC93V7LuJpUDE3IutgiTlUn5EdkiEcVrbQZAsXj"
                            + "/tKijNzOtsx4SrM5R6j7CpsbntGIurXP5UY9j"
                            + "L52nLDIhgS2tZftc9goxNM8ZM9Q1ky89zclcU4OoPou8zTtT/NVDmf3U/ihHAoGBAMTSE5be12gLjsz7RpabXJcoVG+wRo5nvn6P"
                            + ""
                            + "58dKwyQ/+GlboU3aMFHeBDkCZkC6ncc/yMsjgw+wWbNvzxhjDUBGvTNjNyJAQM0vstSJMJp+ciynfGic5k8ciiN"
                            + "/2Nd9qLENeKd4" + "oIRn0LXsBHcKYP/oLFpbFhyJ1a3+BH1Zo3ZL";

        String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvd6hs/wNDClmRFca4sV67dT0/3v0iq2i0Dnsu+E1wzoXp"
                           + ""
                           + "x6xLNepD5L0hNKN5sjGOi6mmidT8D8yo7UZ4O11Hd1BOAMa2q888c/nNWWrOkLRFosAwGQnGi4gbIfGnYHZ/BYHK96"
                           + "/If2UMmc5L"
                           + "ZAWsJfG4sYS4Qv80GH1ACA2sq0/zY0556YYBbivKrasVxynH/+cgBKlaEliBrQj8dusT3IFrzKVVlKy"
                           + "+0CRXZdOZZ/XRtS5ZfQv2"
                           + "Al2r/xabdsEm8WbnumeYKO1HOXhZQfDGpdlYozRo+snQO+NYL5avJkYLzgoL1t8qKgL4ZyQ4eq2lLHBI4Gp2ec5qdBoe2qn3wIDA"
                           + "" + "QAB";
        signatureUtil = SignatureUtilImpl.buildInstance(privateKey, publicKey,
            SignatureUtil.Algorithms.SHA224withRSA);
        data = "这是测试签名";
    }

    @Test
    public void doSign() {
        Assert.assertTrue(signatureUtil.checkSign(data, signatureUtil.sign(data)));
    }
}
```

### parse包
> 提供数据序列化、反序列化，包含json和xml的序列化、反序列化。
#### JsonParser
提供JSON的序列化与反序列化，使用示例：
```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author joe
 * @version 2018.08.09 14:49
 */
public class JsonParserTest {
    JsonParser parser;
    List<User> users;
    String     usersJson;

    @Test
    public void doParse() {
        Assert.assertEquals(parser.toJson(users) , usersJson);
        Arrays.deepEquals(parser.readAsCollection(usersJson, ArrayList.class, User.class).toArray(), users.toArray());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class User {
        private String name;
        private int    age;
        private String alias;
    }

    @Before
    public void init() {
        parser = JsonParser.getInstance();
        int len = 3;
        users = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            User user = new User("user-" + i, i, "joe-" + i);
            users.add(user);
        }
        usersJson = "[{\"name\":\"user-0\",\"age\":0,\"alias\":\"joe-0\"},{\"name\":\"user-1\",\"age\":1,"
                    + "\"alias\":\"joe-1\"},{\"name\":\"user-2\",\"age\":2,\"alias\":\"joe-2\"}]";
    }
}
```
#### XmlParser
提供解析xml的能力，可以将xml反序列化为bean或者将bean序列化为xml，并且可以定制，使用示例：
```java
import java.util.*;

import org.junit.Assert;
import org.junit.Test;

import lombok.Data;

/**
 * XmlParser测试
 *
 * @author joe
 * @version 2018.05.08 10:25
 */
public class XmlParserTest {
    private static final XmlParser PARSER     = XmlParser.getInstance();
    private static final String    NOTHASNULL = "<USER><users2><user><ALIAS>u1</ALIAS><age>0</age><NAME>u1</NAME></user"
                                                + "></users2><users1><ALIAS>u1</ALIAS><age>0</age><NAME>u1</NAME></users1><ALIAS>qiao</ALIAS><userSet"
                                                + "><ALIAS>u2</ALIAS><age>0</age><NAME>u2</NAME></userSet><age>18</age><NAME>joe</NAME></USER>";
    private static final String    HASNULL    = "<USER><users2><user><ALIAS>u1</ALIAS><age>0</age><NAME>u1</NAME></user"
                                                + "></users2><users1><ALIAS>u1</ALIAS><age>0</age><NAME>u1</NAME></users1><ALIAS>qiao</ALIAS><userSet"
                                                + "><ALIAS>u2</ALIAS><age>0</age><NAME>u2</NAME></userSet><age>18</age><NAME>joe</NAME></USER>";
    private static final String    MAP_XML    = "<root><test>test</test><user><users2><user><ALIAS>u1</ALIAS><age>0</age"
                                                + "><NAME>u1</NAME></user></users2><users1><ALIAS>u1</ALIAS><age>0</age><NAME>u1</NAME></users1><ALIAS>qiao"
                                                + "</ALIAS><userSet><ALIAS>u2</ALIAS><age>0</age><NAME>u2</NAME></userSet><age>18</age><NAME>joe</NAME"
                                                + "></user></root>";

    @Test
    public void test() {
        String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www"
                     + ".w3.org/2001/XMLSchema-instance\"\n"
                     + "\txsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache"
                     + ".org/xsd/maven-4.0.0.xsd\">\n" + "\t<modelVersion>4.0.0</modelVersion>\n"
                     + "\t<parent>\n" + "\t\t<groupId>com.fruit.user</groupId>\n"
                     + "\t\t<artifactId>fruit-farm-user</artifactId>\n"
                     + "\t\t<version>1.0</version>\n" + "\t</parent>\n"
                     + "\t<artifactId>fruit-farm-user-service</artifactId>\n"
                     + "\t<packaging>jar</packaging>\n" + "\n" + "\t<dependencies>\n"
                     + "\t\t<!--spring-boot-starter -->\n" + "\t\t<dependency>\n"
                     + "\t\t\t<groupId>org.springframework.boot</groupId>\n"
                     + "\t\t\t<artifactId>spring-boot-starter</artifactId>\n"
                     + "\t\t</dependency>\n" + "\t\t<dependency>\n"
                     + "\t\t\t<groupId>org.springframework.boot</groupId>\n"
                     + "\t\t\t<artifactId>spring-boot-starter-test</artifactId>\n"
                     + "\t\t</dependency>\n" + "\t\t<!--配置中心 -->\n" + "\t\t<dependency>\n"
                     + "\t\t\t<groupId>org.springframework.cloud</groupId>\n"
                     + "\t\t\t<artifactId>spring-cloud-starter-config</artifactId>\n"
                     + "\t\t</dependency>\n" + "\n" + "\t\t<dependency>\n"
                     + "\t\t\t<groupId>com.fruit</groupId>\n"
                     + "\t\t\t<artifactId>fruit-farm-redis</artifactId>\n"
                     + "\t\t\t<version>1.0</version>\n" + "\t\t</dependency>\n"
                     + "\t\t<dependency>\n" + "\t\t\t<groupId>com.fruit.user</groupId>\n"
                     + "\t\t\t<artifactId>fruit-farm-user-sdk</artifactId>\n"
                     + "\t\t\t<version>1.0</version>\n" + "\t\t</dependency>\n"
                     + "\t\t<dependency>\n" + "\t\t\t<groupId>com.fruit.user</groupId>\n"
                     + "\t\t\t<artifactId>fruit-farm-user-mapper</artifactId>\n"
                     + "\t\t\t<version>1.0</version>\n" + "\t\t</dependency>\n"
                     + "\t\t<dependency>\n" + "\t\t\t<groupId>com.fruit</groupId>\n"
                     + "\t\t\t<artifactId>fruit-farm-sys-mapper</artifactId>\n"
                     + "\t\t\t<version>1.0</version>\n" + "\t\t</dependency>\n"
                     + "\t\t<dependency>\n" + "\t\t\t<groupId>com.fruit.goods</groupId>\n"
                     + "\t\t\t<artifactId>fruit-farm-goods-sdk</artifactId>\n"
                     + "\t\t\t<version>1.0</version>\n" + "\t\t</dependency>\n"
                     + "\t\t<dependency>\n" + "\t\t\t<groupId>org.apache.httpcomponents</groupId>\n"
                     + "\t\t\t<artifactId>httpclient</artifactId>\n" + "\t\t</dependency>\n"
                     + "\t\t<dependency>\n" + "\t\t\t<groupId>com.alibaba</groupId>\n"
                     + "\t\t\t<artifactId>dubbo</artifactId>\n" + "\t\t\t<exclusions>\n"
                     + "\t\t\t\t<exclusion>\n"
                     + "\t\t\t\t\t<groupId>org.springframework</groupId>\n"
                     + "\t\t\t\t\t<artifactId>spring</artifactId>\n" + "\t\t\t\t</exclusion>\n"
                     + "\t\t\t\t<exclusion>\n" + "\t\t\t\t\t<groupId>javax.servlet</groupId>\n"
                     + "\t\t\t\t\t<artifactId>javax.servlet-api</artifactId>\n"
                     + "\t\t\t\t</exclusion>\n" + "\t\t\t</exclusions>\n" + "\t\t</dependency>\n"
                     + "\n" + "\t\t<dependency>\n"
                     + "\t\t\t<groupId>org.apache.zookeeper</groupId>\n"
                     + "\t\t\t<artifactId>zookeeper</artifactId>\n" + "\t\t\t<exclusions>\n"
                     + "\t\t\t\t<exclusion>\n" + "\t\t\t\t\t<groupId>org.slf4j</groupId>\n"
                     + "\t\t\t\t\t<artifactId>slf4j-log4j12</artifactId>\n"
                     + "\t\t\t\t</exclusion>\n" + "\t\t\t</exclusions>\n" + "\t\t</dependency>\n"
                     + "\t\t<dependency>\n" + "\t\t\t<groupId>com.github.sgroschupf</groupId>\n"
                     + "\t\t\t<artifactId>zkclient</artifactId>\n" + "\t\t</dependency>\n"
                     + "\t\t<dependency>\n" + "\t\t\t<groupId>com.aliyun</groupId>\n"
                     + "\t\t\t<artifactId>aliyun-java-sdk-core</artifactId>\n"
                     + "\t\t</dependency>\n" + "\t\t<dependency>\n"
                     + "\t\t\t<groupId>com.aliyun</groupId>\n"
                     + "\t\t\t<artifactId>aliyun-java-sdk-dysmsapi</artifactId>\n"
                     + "\t\t</dependency>\n" + "\t\t<!-- oss -->\n" + "\t\t<dependency>\n"
                     + "\t\t\t<groupId>com.aliyun.oss</groupId>\n"
                     + "\t\t\t<artifactId>aliyun-sdk-oss</artifactId>\n" + "\t\t</dependency>\n"
                     + "\n" + "\t\t<!-- 极光推送 开始 -->\n" + "\t\t<dependency>\n"
                     + "\t\t\t<groupId>cn.jpush.api</groupId>\n"
                     + "\t\t\t<artifactId>jpush-client</artifactId>\n" + "\t\t</dependency>\n"
                     + "\t\t<dependency>\n" + "\t\t\t<groupId>cn.jpush.api</groupId>\n"
                     + "\t\t\t<artifactId>jiguang-common</artifactId>\n" + "\t\t</dependency>\n"
                     + "\t\t<dependency>\n" + "\t\t\t<groupId>io.netty</groupId>\n"
                     + "\t\t\t<artifactId>netty-all</artifactId>\n"
                     + "\t\t\t<scope>compile</scope>\n" + "\t\t</dependency>\n"
                     + "\t\t<dependency>\n" + "\t\t\t<groupId>com.google.code.gson</groupId>\n"
                     + "\t\t\t<artifactId>gson</artifactId>\n" + "\t\t</dependency>\n"
                     + "\t\t<!-- 极光推送 结束 -->\n" + "\t\t<!-- 小米推送 开始 -->\n" + "\t\t<dependency>\n"
                     + "\t\t\t<groupId>com.xiaomi</groupId>\n"
                     + "\t\t\t<artifactId>json-simple</artifactId>\n" + "\t\t</dependency>\n"
                     + "\t\t<dependency>\n" + "\t\t\t<groupId>com.xiaomi</groupId>\n"
                     + "\t\t\t<artifactId>MiPush_SDK_Server</artifactId>\n" + "\t\t</dependency>\n"
                     + "\t\t<!-- 小米推送 结束 -->\n" + "\t\t<dependency>\n"
                     + "\t\t\t<groupId>org.springframework.boot</groupId>\n"
                     + "\t\t\t<artifactId>spring-boot-starter-jdbc</artifactId>\n"
                     + "\t\t</dependency>\n" + "\t\t<dependency>\n"
                     + "\t\t\t<groupId>com.github.JoeKerouac</groupId>\n"
                     + "\t\t\t<artifactId>utils</artifactId>\n" + "\t\t</dependency>\n"
                     + "\t</dependencies>\n" + "\n" + "\t<build>\n"
                     + "\t\t<finalName>user-service</finalName>\n" + "\t\t<plugins>\n"
                     + "\t\t\t<!--jar包构建插件 -->\n" + "\t\t\t<plugin>\n"
                     + "\t\t\t\t<groupId>org.springframework.boot</groupId>\n"
                     + "\t\t\t\t<artifactId>spring-boot-maven-plugin</artifactId>\n"
                     + "\t\t\t</plugin>\n" + "\t\t</plugins>\n" + "\t</build>\n" + "\n"
                     + "</project>";

        Pom pom = PARSER.parse(xml, Pom.class);
        System.out.println(pom);
        System.out.println(NOTHASNULL);

        Pom p = new Pom();
        Dependency dependency = new Dependency();
        dependency.setArtifactId("123");
        dependency.setGroupId("123");
        List<Dependency> list = new ArrayList<>();
        list.add(dependency);
        list.add(dependency);
        p.setDependencies(list);

        System.out.println(PARSER.toXml(p));
    }

    @Data
    static class Pom {
        @XmlNode(general = Dependency.class)
        private List<Dependency> dependencies;
        private String           artifactId;
    }

    @Data
    static class Dependency {
        private String groupId;
        private String artifactId;
        private String version;
    }

    @Test
    public void doMapToXml() {
        User user = build();
        Map<String, Object> map = new HashMap<>();
        map.put("test", "test");
        map.put("user", user);
        String xml = PARSER.toXml(map);
        Assert.assertTrue(xml.equals(MAP_XML));
    }

    @Test
    public void doToXml() {
        User user = build();

        String xml = PARSER.toXml(user, "USER", true);
        Assert.assertEquals(xml, HASNULL);
        xml = PARSER.toXml(user, "USER", false);
        Assert.assertEquals(xml, NOTHASNULL);
    }

    @Test
    public void doParse() {
        User user = build();
        User u1 = PARSER.parse(NOTHASNULL, User.class);
        Assert.assertEquals(user, u1);
        User u2 = PARSER.parse(HASNULL, User.class);
        Assert.assertEquals(user, u2);
    }

    private User build() {
        User user = new User();
        user.setName("joe");
        user.setOtherName("qiao");
        user.setAge(18);
        List<User> list = new ArrayList<>();
        User u1 = new User();
        u1.setName("u1");
        u1.setOtherName("u1");
        list.add(u1);
        Set<User> set = new HashSet<>();
        User u2 = new User();
        u2.setName("u2");
        u2.setOtherName("u2");
        set.add(u2);

        user.setUsers1(list);
        user.setUsers2(list);
        user.setUserSet(set);
        return user;
    }

    @Data
    static class User {
        @XmlNode(name = "NAME")
        private String     name;
        @XmlNode(name = "ALIAS")
        private String     otherName;
        @XmlNode(ignore = true)
        private String     other = "abc";
        private int        age;
        /**
         * 集合类型必须加general字段
         */
        @XmlNode(general = User.class)
        private List<User> users1;
        /**
         * 当添加arrayRoot选项时，首先会创建一个users2节点，然后在users2节点中会添加多个以user为根节点的User数据
         */
        @XmlNode(general = User.class, arrayRoot = "user")
        private List<User> users2;
        @XmlNode(general = User.class)
        private Set<User>  userSet;
    }
}
```
### common包
> 提供一些例如字符串、IO操作等常用工具
#### IOUtils
提供对IO流的常用操作封装。
#### BeanUtils
提供对bean的常用操作，例如反射获取内部私有属性、复制bean（简单的浅复制）等。
#### DateUtil
日期工具类，提供对日期的操作，例如获取格式化日期字符串、日期加减、判断是否当天等。
#### StringUtils
String工具类，提供常用的String操作，例如判断空、替换指定区间字符串、求两个字符串的最长公共子序列等。
#### TelnetServer
用于快速建立telnet-server。