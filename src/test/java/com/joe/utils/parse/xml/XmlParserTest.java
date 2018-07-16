package com.joe.utils.parse.xml;

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
