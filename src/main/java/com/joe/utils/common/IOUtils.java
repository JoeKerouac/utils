package com.joe.utils.common;

import com.joe.utils.collection.ByteArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * 常用IO操作工具类
 *
 * @author joe
 */
public class IOUtils {
    private static final Logger logger = LoggerFactory.getLogger(IOUtils.class);

    /**
     * 将流中的数据读取为字符串（缓冲区大小为256byte）
     *
     * @param in      输入流
     * @param charset 字符串编码
     * @return 流中的数据
     * @throws IOException IO异常
     */
    public static String read(InputStream in, String charset) throws IOException {
        logger.debug("开始从流中读取内容");
        charset = charset == null ? "UTF8" : charset;
        int bufSize = 256;
        logger.debug("文本编码为：{}，缓冲区大小为{}byte", charset, bufSize);
        return new String(read(in, bufSize), charset);
    }

    /**
     * 将流读取为byte数组
     *
     * @param in 输入流
     * @return 字节数组
     * @throws IOException IO异常
     */
    public static byte[] read(InputStream in) throws IOException {
        return read(in, 256);
    }

    /**
     * 将文本保存到本地文件
     *
     * @param data    文本
     * @param charset 文本的字符集，不填默认为UTF8
     * @param addr    保存本地的路径，包含文件名，例如D://a.txt
     * @throws IOException IO异常
     */
    public static void saveAsFile(String data, String charset, String addr) throws IOException {
        charset = StringUtils.isEmpty(charset) ? "UTF8" : charset;
        saveAsFile(data.getBytes(charset), addr);
    }

    /**
     * 保存数据到本地
     *
     * @param data 数据
     * @param addr 保存本地的路径，包含文件名，例如D://a.txt
     * @throws IOException IO异常
     */
    public static void saveAsFile(byte[] data, String addr) throws IOException {
        logger.debug("将字节数据保存到本地文件");
        File file = new File(addr);
        if (!file.exists()) {
            if (!file.getParentFile().mkdirs()) {
                logger.error("创建目录{}失败", addr);
            }
        }
        logger.debug("保存路径为：{}", file.getAbsolutePath());
        FileOutputStream out = new FileOutputStream(file);
        out.write(data);
        out.flush();
        out.close();
        logger.debug("文件保存完毕");
    }

    /**
     * 将流读取为byte数组
     *
     * @param in      输入流
     * @param bufSize 自定义缓冲区大小
     * @return 字节数组
     * @throws IOException IO异常
     */
    public static byte[] read(InputStream in, int bufSize) throws IOException {
        logger.debug("开始从流中读取数据，缓冲区大小为{}byte", bufSize);
        ByteArray array = new ByteArray();
        int len;
        byte[] buffer = new byte[bufSize];
        while ((len = in.read(buffer, 0, buffer.length)) != -1) {
            array.append(buffer, 0, len);
        }
        logger.debug("读取完毕");
        return array.getData();
    }

    /**
     * 将byte数据转换为输入流的形式
     *
     * @param data byte数据
     * @return 输入流
     */
    public static InputStream convert(byte[] data) {
        return new ByteArrayInputStream(data);
    }
}
