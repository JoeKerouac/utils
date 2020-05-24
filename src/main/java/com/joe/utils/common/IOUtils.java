package com.joe.utils.common;

import java.io.*;

import com.joe.utils.collection.ByteArray;
import com.joe.utils.common.string.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 常用IO操作工具类
 *
 * @author joe
 */
@Slf4j
public class IOUtils {

    private static final int DEFAULT_BUF_SIZE = 2048;

    /**
     * 将文件读取为byte数组
     *
     * @param file 文件
     * @return 字节数组
     * @throws IOException IO异常
     */
    public static byte[] read(File file) throws IOException {
        return read(new FileInputStream(file), DEFAULT_BUF_SIZE, true);
    }

    /**
     * 将文件中的数据读取为字符串（缓冲区大小为{@link #DEFAULT_BUF_SIZE}byte）
     *
     * @param file 文件
     * @param charset 字符串编码
     * @return 流中的数据
     * @throws IOException IO异常
     */
    public static String read(File file, String charset) throws IOException {
        return read(file, charset, DEFAULT_BUF_SIZE);
    }

    /**
     * 将文件中的数据读取为字符串（缓冲区大小为{@link #DEFAULT_BUF_SIZE}byte）
     *
     * @param file 文件
     * @param charset 字符串编码
     * @param bufSize 自定义缓冲区大小
     * @return 流中的数据
     * @throws IOException IO异常
     */
    public static String read(File file, String charset, int bufSize) throws IOException {
        return read(new FileInputStream(file), charset, bufSize, true);
    }

    /**
     * 将流读取为byte数组
     *
     * @param in 输入流
     * @return 字节数组
     * @throws IOException IO异常
     */
    public static byte[] read(InputStream in) throws IOException {
        return read(in, DEFAULT_BUF_SIZE);
    }

    /**
     * 将流中的数据读取为字符串（缓冲区大小为256byte）
     *
     * @param in      输入流
     * @param charset 字符串编码
     * @return 流中的数据
     * @throws IOException IO异常
     */
    public static String read(InputStream in, String charset) throws IOException {
        return read(in, charset, DEFAULT_BUF_SIZE);
    }

    /**
     * 将流中的数据读取为字符串（缓冲区大小为256byte）
     *
     * @param in      输入流
     * @param charset 字符串编码
     * @param bufSize 自定义缓冲区大小
     * @return 流中的数据
     * @throws IOException IO异常
     */
    public static String read(InputStream in, String charset, int bufSize) throws IOException {
        return read(in, charset, bufSize, false);
    }

    /**
     * 将流中的数据读取为字符串（缓冲区大小为256byte）
     *
     * @param in      输入流
     * @param charset 字符串编码
     * @param bufSize 自定义缓冲区大小
     * @param close 读取完毕是否关闭流
     * @return 流中的数据
     * @throws IOException IO异常
     */
    public static String read(InputStream in, String charset, int bufSize,
                              boolean close) throws IOException {
        log.debug("开始从流中读取内容");
        charset = charset == null ? "UTF8" : charset;
        log.debug("文本编码为：{}，缓冲区大小为{}byte", charset, bufSize);
        return new String(read(in, bufSize, close), charset);
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
        return read(in, bufSize, false);
    }

    /**
     * 将流读取为byte数组
     *
     * @param in      输入流
     * @param bufSize 自定义缓冲区大小
     * @param close 是否关闭流
     * @return 字节数组
     * @throws IOException IO异常
     */
    public static byte[] read(InputStream in, int bufSize, boolean close) throws IOException {
        log.debug("开始从流中读取数据，缓冲区大小为{}byte", bufSize);
        ByteArray array = new ByteArray();
        int len;
        byte[] buffer = new byte[bufSize];
        while ((len = in.read(buffer, 0, buffer.length)) != -1) {
            array.append(buffer, 0, len);
        }
        log.debug("读取完毕");
        if (close) {
            in.close();
        }
        return array.getData();
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
        log.debug("将字节数据保存到本地文件");
        File file = new File(addr);
        if (!file.exists() && !file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                log.error("创建目录{}失败", addr);
            }
        }
        log.debug("保存路径为：{}", file.getAbsolutePath());
        FileOutputStream out = new FileOutputStream(file);
        out.write(data);
        out.flush();
        out.close();
        log.debug("文件保存完毕");
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

    /**
     * 关闭closeable，忽略抛出的异常
     *
     * @param closeable closeable
     */
    public static void closeQuietly(AutoCloseable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
            log.warn("关闭[{}]时异常，忽略", closeable, e);
        }
    }
}
