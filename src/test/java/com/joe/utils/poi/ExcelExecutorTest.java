package com.joe.utils.poi;

import lombok.Data;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author joe
 * @version 2018.06.15 14:53
 */
public class ExcelExecutorTest {
    List<User> list = new ArrayList<>();

    @Before
    public void init() {
        List<User> list = new ArrayList<>();
        User user = new User();
        user.setAge(1);
        user.setName("乔冠华");
        user.setSex("男");
        list.add(user);
    }

    @Test
    public void doWriteStream() throws IOException{
        File file = new File("user.xlsx");
        Assert.assertTrue(!file.exists());

        FileOutputStream outputStream = new FileOutputStream(file);
        ExcelExecutor.getInstance().writeToExcel(list, true, outputStream);
        outputStream.close();

        Assert.assertTrue(file.exists());
        file.delete();
    }

    @Test
    public void doWriteFile() throws IOException{
        File file = new File("user.xlsx");
        Assert.assertTrue(!file.exists());
        ExcelExecutor.getInstance().writeToExcel(list, true, file.getAbsolutePath());
        Assert.assertTrue(file.exists());
        file.delete();
    }

    @Data
    static class User extends People{
        @ExcelColumn("姓名")
        private String name;
        @ExcelColumn("年龄")
        private int age;
    }

    @Data
    static class People {
        @ExcelColumn("性别")
        private String sex;
    }
}
