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
}
