package com.joe.utils.protocol;

/**
 * Datagram中使用的常量
 *
 * @author JoeKerouac
 * @version 2019年11月19日 11:12
 */
public class DatagramConst {

    public static class Version{

        /**
         * V1版本
         */
        public static final byte V1 = 1;
    }

    /**
     * Datagram类型
     */
    public static class Type{

        /**
         * 心跳包类型
         */
        public static final byte    HEARTBEAT      = 0;

        /**
         * MVC数据类型
         */
        public static final byte    MVC            = 1;

        /**
         * 文件上传数据类型
         */
        public static final byte    FILE           = 2;

        /**
         * ACK数据类型
         */
        public static final byte    ACK            = 3;

        /**
         * BACK数据类型
         */
        public static final byte    BACK           = 4;
    }

    /**
     * 数据定位
     */
    public static class Position{

        /**
         * 数据报的报头长度
         */
        public static final int     HEADER_LEN     = 56;

        /**
         * 版本号字段的位置
         */
        public static final int     VERSION_INDEX  = 0;

        /**
         * 请求头中长度字段起始位置
         */
        public static final int     LEN_OFFSET     = 1;

        /**
         * 请求头中长度字段的长度
         */
        public static final int     LEN_LIMIT      = 4;

        /**
         * 数据报类型字段的位置
         */
        public static final int     TYPE_INDEX     = 5;

        /**
         * 数据报的最大长度，包含请求头和请求体
         */
        public static final int     MAX_LENGTH     = Integer.MAX_VALUE;

        /**
         * 字符集起始位置
         */
        public static final int     CHARSET_OFFSET = 6;

        /**
         * 字符集最大长度
         */
        public static final int     CHARSET_MAX    = 10;
    }
}
