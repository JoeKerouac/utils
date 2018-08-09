package com.joe.utils.parse.json;

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
