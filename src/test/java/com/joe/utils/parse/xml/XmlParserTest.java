package com.joe.utils.parse.xml;

import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * XmlParser测试
 *
 * @author joe
 * @version 2018.05.08 10:25
 */
public class XmlParserTest {
    private static final XmlParser PARSER = XmlParser.getInstance();
    private static final String NOTHASNULL = "<USER><NAME>joe</NAME><ALIAS>qiao</ALIAS><age>18</age><users1><NAME>u1" +
            "</NAME><ALIAS>u1</ALIAS><age>0</age></users1><users2><user><NAME>u1</NAME><ALIAS>u1</ALIAS><age>0</age" +
            "></user></users2><userSet><NAME>u2</NAME><ALIAS>u2</ALIAS><age>0</age></userSet></USER>";
    private static final String HASNULL = "<USER><NAME>joe</NAME><ALIAS>qiao</ALIAS><age>18</age><users1><NAME>u1" +
            "</NAME><ALIAS>u1</ALIAS><age>0</age></users1><users2><user><NAME>u1</NAME><ALIAS>u1</ALIAS><age>0</age" +
            "></user></users2><userSet><NAME>u2</NAME><ALIAS>u2</ALIAS><age>0</age></userSet></USER>";


    @Test
    public void doToXml() {
        User user = build();

        String xml = PARSER.toXml(user, "USER", true);
        Assert.assertEquals(xml , HASNULL);
        xml = PARSER.toXml(user, "USER", false);
        Assert.assertEquals(xml , NOTHASNULL);
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
        private String name;
        @XmlNode(name = "ALIAS")
        private String otherName;
        @XmlNode(ignore = true)
        private String other = "abc";
        private int age;
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
        private Set<User> userSet;
    }
}
