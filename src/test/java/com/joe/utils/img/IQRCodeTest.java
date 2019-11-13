package com.joe.utils.img;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

/**
 * 二维码工具类测试
 *
 * @author joe
 * @version 2018.05.24 18:47
 */
public class IQRCodeTest {
    @Test
    public void doCreate() throws IOException {
        IQRCode.create("123456", new ByteArrayOutputStream(), 100, 100);
    }

    @Test
    public void createImg() throws IOException {
        BufferedImage image = IQRCode.createImg("123456", 100, 100);
        Assert.assertNotNull(image);
    }
}
