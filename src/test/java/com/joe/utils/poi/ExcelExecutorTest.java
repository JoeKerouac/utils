package com.joe.utils.poi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.joe.utils.poi.data.StringDataWriter;

import lombok.Data;

/**
 * @author joe
 * @version 2018.06.15 14:53
 */
public class ExcelExecutorTest {
    List<User> list = new ArrayList<>();

    @Before
    public void init() {
        User user = new User();
        user.setAge(1);
        user.setName("乔冠华");
        user.setSex("男");
        list.add(user);
    }

    @Test
    public void doWriteStream() {
        // 测试写入流
        fileTest(file -> {
            try {
                FileOutputStream outputStream = new FileOutputStream(file);
                ExcelExecutor.getInstance().writeToExcel(list, true, outputStream);
                outputStream.close();
            } catch (IOException e) {
                Assert.assertTrue("发生IO异常", e == null);
            }
        });
    }

    @Test
    public void doWriteFile() {
        // 测试写入文件
        fileTest(file -> {
            try {
                ExcelExecutor.getInstance().writeToExcel(list, true, file.getAbsolutePath());
            } catch (IOException e) {
                Assert.assertNotNull("发生IO异常", e);
            }
        });
    }

    @Test
    public void doStyle() {
        // 测试样式
        fileTest(file -> {
            try {
                SXSSFWorkbook wb = new SXSSFWorkbook(100);
                ExcelExecutor.getInstance().writeToExcel(list, true, wb);
                WorkBookAccesser workBookAccesser = new WorkBookAccesser(wb);
                // 合并单元格
                workBookAccesser.mergedRowRegion(4, 0, 3);
                CellStyleAccesser accesser = workBookAccesser.getCellStyleAccesser(0, 0);
                accesser.bold(true);
                accesser.setFontSize((short)100);
                accesser.color(HSSFColor.HSSFColorPredefined.BLUE);
                OutputStream stream = new FileOutputStream(file);
                wb.write(stream);
                stream.close();
                wb.dispose();
                wb.close();
            } catch (IOException e) {
                Assert.assertNotNull("发生IO异常", e);
            }
        });
    }

    @Test
    public void doDataWriter() {
        fileTest(file -> {
            try {
                ExcelExecutor executor = ExcelExecutor.getInstance();

                // 使用自定义的StringDataWriter替换系统默认的StringDataWriter
                executor.registerDataWriter(String.class, new StringDataWriter() {
                    @Override
                    public void write(Cell cell, String data) {
                        // 将第一行加粗设置字号为20并且颜色设置为红色
                        if (cell.getRowIndex() == 0) {
                            CellStyleAccesser accesser = CellStyleAccesser.build(cell);
                            accesser.bold(true).setFontSize((short)20).color(HSSFColor.HSSFColorPredefined.RED);
                        }
                        cell.setCellValue(data);
                    }
                });
                executor.writeToExcel(list, true, file.getAbsolutePath());
            } catch (IOException e) {
                Assert.assertNotNull("发生IO异常", e);
            }
        });
    }

    private void fileTest(Consumer<File> function) {
        File file = new File("user-" + Math.random() + ".xlsx");
        try {
            Assert.assertTrue(!file.exists());

            function.accept(file);

            Assert.assertTrue(file.exists());
        } finally {
            file.delete();
        }
    }

    @Data
    static class User extends People {
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
