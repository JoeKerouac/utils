package com.joe.utils.codec;

/**
 * @author joe
 * @version 2018.07.11 17:41
 */
public class Hex {
    /**
     * Used to build output as Hex
     */
    private static final char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                                                 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * Used to build output as Hex
     */
    private static final char[] DIGITS_UPPER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                                                 'A', 'B', 'C', 'D', 'E', 'F' };

    /**
     * 将十六进制字符串解析为byte数组
     * @param hexStr 16进制字符串
     * @return byte数组
     */
    public static byte[] decodeHex(final String hexStr) {
        return decodeHex(hexStr.getBytes());
    }

    /**
     * 将十六进制字符解析为byte数组
     * @param hexDatas 16进制字符
     * @return byte数组
     */
    public static byte[] decodeHex(final char[] hexDatas) {
        if (hexDatas.length % 2 != 0) {
            throw new IllegalArgumentException("非法的16进制字符");
        }
        byte[] data = new byte[hexDatas.length >> 1];
        for (int i = 0; i < hexDatas.length; i += 2) {
            data[i >> 1] = (byte) ((indexAt((byte) hexDatas[i]) << 4)
                                   | indexAt((byte) hexDatas[i + 1]));
        }
        return data;
    }

    /**
     * 将十六进制形式字符解析为byte数组
     * @param hexDatas 16进制字符byte形式
     * @return byte数组
     */
    public static byte[] decodeHex(final byte[] hexDatas) {
        if (hexDatas.length % 2 != 0) {
            throw new IllegalArgumentException("非法的16进制字符");
        }
        byte[] data = new byte[hexDatas.length >> 1];
        for (int i = 0; i < hexDatas.length; i += 2) {
            data[i >> 1] = (byte) ((indexAt(hexDatas[i]) << 4) | indexAt(hexDatas[i + 1]));
        }
        return data;
    }

    /**
     * 将byte数组转换为16进制字符数组
     *
     * @param data    数据
     * @param toLower 是否转小写，true表示小写，false表示大写
     * @return 16进制字符数组
     */
    public static char[] encodeHex(final byte[] data, boolean toLower) {
        return encodeHex(data, toLower ? DIGITS_LOWER : DIGITS_UPPER);
    }

    /**
     * 将byte数组转换为16进制字符数组
     *
     * @param data     数据
     * @param toDigits 十六进制对应的字符数组
     * @return 16进制字符数组
     */
    private static char[] encodeHex(final byte[] data, final char[] toDigits) {
        final int l = data.length;
        final char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    /**
     * 获取16进制的char在数组{@link #DIGITS_LOWER}或者{@link #DIGITS_UPPER}中的位置
     * @param c char
     * @return 数组中的位置
     */
    private static int indexAt(byte c) {
        //0-9对应的char范围是48-57；A-Z对应的char范围是65-90；a-z对应的char范围是97-122
        if (c < 58) {
            return (c - 48);
        } else if (c < 91) {
            return (c - 55);
        } else {
            return (c - 87);
        }
    }
}
