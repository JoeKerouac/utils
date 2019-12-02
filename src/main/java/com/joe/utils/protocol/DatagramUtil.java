package com.joe.utils.protocol;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import com.joe.utils.common.Assert;
import com.joe.utils.common.Tools;
import com.joe.utils.common.string.StringFormater;
import com.joe.utils.protocol.exception.DataOutOfMemory;
import com.joe.utils.protocol.exception.IllegalDataException;
import com.joe.utils.protocol.exception.IllegalRequestException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据报工具
 *
 * @author joe
 */
@Slf4j
public class DatagramUtil {

    /**
     * 当前系统默认字符集
     */
    private static final String DEFAULT_CHARSET;

    /**
     * 系统字符集的byte数据（长度10byte，不足的后边补零）
     */
    private static final byte[] DEFAULT_CHARSET_DATA;

    /**
     * 数据报数据除去请求头的最大长度
     */
    private static final int    BODY_MAX_LENGTH;

    static {
        BODY_MAX_LENGTH = DatagramConst.Position.MAX_LENGTH - DatagramConst.Position.HEADER_LEN;
        DEFAULT_CHARSET = Charset.defaultCharset().name();
        byte[] charsetBytes = DEFAULT_CHARSET.getBytes();
        int charsetLen = charsetBytes.length;
        if (charsetLen > 10) {
            throw new DataOutOfMemory("数据报字符集长度最大为10byte，当前系统默认字符集超过该长度");
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream(10);
        stream.write(charsetBytes, 0, charsetLen);
        while (charsetLen < 10) {
            charsetLen++;
            stream.write(0);
        }
        DEFAULT_CHARSET_DATA = stream.toByteArray();
    }

    /**
     * 根据要发送的数据构建数据报（编码采用当前系统默认编码）
     *
     * @param body    要发送的数据
     * @param type    数据报类型（0：心跳包；1：内置MVC数据处理器数据类型；2：文件传输；3：ACK；4：后端主动发往前端的数据）
     * @param version 数据报版本
     * @return 构建好的数据报对象
     * @throws DataOutOfMemory 当数据长度过长时会抛出该异常
     */
    public static Datagram build(final byte[] body, final byte type,
                                 final byte version) throws DataOutOfMemory {
        final int dataLen;
        if (body != null && body.length != 0) {
            // 获取要发送的数据的长度
            dataLen = body.length;
            log.info("要构建的数据报的body长度为：{}", dataLen);
        } else {
            dataLen = 0;
            log.info("要构建一个空的数据报");
        }

        if (log.isDebugEnabled()) {
            log.debug("要发送的数据为：{}", Arrays.toString(body));
        }
        if (dataLen > BODY_MAX_LENGTH) {
            // 数据报超出最大值
            log.error("数据报数据长度超过最大值：{}", BODY_MAX_LENGTH);
            throw new DataOutOfMemory(String.format("数据长度超过最大值%d", BODY_MAX_LENGTH));
        }

        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(
            dataLen + DatagramConst.Position.HEADER_LEN,
            dataLen + DatagramConst.Position.HEADER_LEN);

        // 一个字节的版本号
        buffer.writeByte(Byte.toUnsignedInt(version));
        // 四个字节的body长度
        buffer.writeBytes(splitToByte(dataLen));
        // 一个字节的数据类型
        buffer.writeByte(Byte.toUnsignedInt(type));
        // 十个字节的字符集，字符集为当前系统默认字符集不可更改，不足十个字节的用0填充
        buffer.writeBytes(DEFAULT_CHARSET_DATA);

        //添加ID字段
        byte[] idDatas = Tools.createUUID().getBytes();
        int idLen = idDatas.length;
        buffer.writeBytes(idDatas);
        //ID不足40byte的补0
        for (int i = idLen; i < 40; i++) {
            buffer.writeByte(0);
        }

        if (dataLen != 0) {
            // 填充数据
            buffer.writeBytes(body);
        }

        Datagram datagram = new Datagram(ByteBufUtil.getBytes(buffer), dataLen, version,
            DEFAULT_CHARSET, type, idDatas);
        // 最后要释放
        buffer.release();
        if (log.isDebugEnabled()) {
            log.debug("转换后的数据报是：{}", datagram);
        }
        return datagram;
    }

    /**
     * 数据报解析，将byte数组解析为数据报
     *
     * @param data 数据报的byte数组
     * @return 从data中解析的数据报对象
     * @throws IllegalRequestException 正常情况不会抛该异常，当请求非法时可能抛出该异常
     */
    public static Datagram decode(final byte[] data) throws IllegalRequestException {
        return decode(data, false);
    }

    /**
     * 数据报解析，将byte数组解析为数据报
     *
     * @param data     数据报的byte数组
     * @param allowErr 是否允许数据报错误，true表示允许，当允许该错误的时候，如果数据报实际长度大于报头的长度将采用报头的长度，该参数主要用于解析UDP数据
     * @return 从data中解析的数据报对象
     * @throws IllegalRequestException 正常情况不会抛该异常，当请求非法时可能抛出该异常
     */
    public static Datagram decode(final byte[] data,
                                  boolean allowErr) throws IllegalRequestException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("要解析的数据为：{}", Arrays.toString(data));
            }
            // 字符集数据
            int end = DatagramConst.Position.CHARSET_OFFSET;
            for (int i = 0; i < DatagramConst.Position.CHARSET_MAX; end += i, i++) {
                if (data[end] == 0) {
                    break;
                }
            }
            // 字符集
            final String charset = new String(data, DatagramConst.Position.CHARSET_OFFSET,
                end - DatagramConst.Position.CHARSET_OFFSET);
            // 版本号
            final byte version = data[DatagramConst.Position.VERSION_INDEX];
            // 数据报数据类型
            final byte type = data[DatagramConst.Position.TYPE_INDEX];
            // 长度
            final int len = mergeToInt(data);
            log.debug("要解析的数据报的字符集为：{}，版本号为：{}，数据报类型为：{}", charset, version, type);

            byte[] buffer;
            if ((data.length - DatagramConst.Position.HEADER_LEN) > len) {
                log.warn("数据报head中的长度字段为：{}，数据报body的实际长度为：{}", len,
                    data.length - DatagramConst.Position.HEADER_LEN);
                if (allowErr) {
                    buffer = new byte[len + DatagramConst.Position.HEADER_LEN];
                    System.arraycopy(data, 0, buffer, 0, buffer.length);
                } else {
                    throw new IllegalDataException("数据报head中的长度字段为：" + len + "，数据报body的实际长度为："
                                                   + (data.length
                                                      - DatagramConst.Position.HEADER_LEN));
                }
            } else if ((data.length - DatagramConst.Position.HEADER_LEN) < len) {
                log.error("数据报head中的长度字段为：{}，数据报body的实际长度为：{}", len,
                    data.length - DatagramConst.Position.HEADER_LEN);
                throw new IllegalDataException("数据报head中的长度字段为：" + len + "，数据报body的实际长度为："
                                               + (data.length - DatagramConst.Position.HEADER_LEN));
            } else {
                buffer = data;
            }

            log.debug("获取id字段");
            byte[] idByte = new byte[40];
            System.arraycopy(data, 16, idByte, 0, idByte.length);
            String id = new String(idByte);
            log.debug("数据报ID为：{}", id);

            // 有可能是空报文的数据报
            Datagram datagram;
            if (len == 0) {
                log.debug("要解析的数据中head标志body长度为0，直接返回一个空body的datagram对象");
                datagram = new Datagram(buffer, len, version, charset, type, idByte);
            } else {
                // 真实的业务数据
                byte[] body = new byte[len];
                System.arraycopy(buffer, DatagramConst.Position.HEADER_LEN, body, 0, body.length);
                datagram = new Datagram(buffer, len, version, charset, type, idByte);
            }
            if (log.isDebugEnabled()) {
                log.debug("封装好的数据报body为：{}", datagram);
            }
            return datagram;
        } catch (Exception e) {
            log.error("数据报解析错误，错误原因：", e);
            throw new IllegalRequestException(e);
        }
    }

    /**
     * 读取合并数据报的长度字段
     *
     * @param data 数据
     * @return 长度
     */
    public static int mergeToInt(byte[] data) {
        if (data.length < DatagramConst.Position.HEADER_LEN) {
            log.warn("要读取长度的数据报报头不完整");
            throw new IllegalDataException("要读取长度的数据报报头不完整:" + Arrays.toString(data));
        } else {
            return mergeToInt(data, DatagramConst.Position.LEN_OFFSET);
        }
    }

    /**
     * 将一个int拆分为四个byte
     *
     * @param data int类型的参数
     * @return byte类型的数组
     */
    public static byte[] splitToByte(int data) {
        long len = Integer.toUnsignedLong(data);
        byte[] b = new byte[DatagramConst.Position.LEN_LIMIT];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) (len >> ((b.length - i - 1) * 8));
        }
        return b;
    }

    /**
     * 将四个byte合并为一个int
     *
     * @param data  数据源
     * @param start 要合并的byte数据的起始位置
     * @return 四个byte转换为的一个int
     */
    public static int mergeToInt(byte[] data, int start) {
        Assert.isTrue(data.length >= start + 4,
            StringFormater.simpleFormat("合并数据错误，数据：{0}", Arrays.toString(data)));
        return (Byte.toUnsignedInt(data[start]) << 24) | (Byte.toUnsignedInt(data[start + 1]) << 16)
               | (Byte.toUnsignedInt(data[start + 2]) << 8) | Byte.toUnsignedInt(data[start + 3]);
    }
}
