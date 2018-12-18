package com.joe.utils.serialize.xml;

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
    public void doMapToXml() {
        User user = build();
        Map<String, Object> map = new HashMap<>();
        map.put("test", "test");
        map.put("user", user);
        String xml = PARSER.toXml(map);
        Assert.assertEquals(xml, MAP_XML);
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
