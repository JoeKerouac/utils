package com.joe.utils.secure;

/**
 * 数据摘要工具
 *
 * @author joe
 * @version 2018.07.12 13:40
 */
public interface MessageDigestUtil {
    /**
     * 获取数据摘要
     *
     * @param data 数据
     * @return 对应的摘要（转为了16进制字符串）
     */
    String digest(String data);

    /**
     * 获取数据摘要
     *
     * @param data 数据
     * @return 对应的摘要
     */
    byte[] digest(byte[] data);

    /**
     * 摘要算法
     */
    enum Algorithms {
                     MD2("MD2"), MD5("MD5"), SHA1("SHA-1"), SHA224("SHA-224"), SHA256("SHA-256"), SHA384("SHA-384"), SHA512("SHA-512");
        private String algorithms;

        Algorithms(String algorithms) {
            this.algorithms = algorithms;
        }

        /**
         * 获取摘要算法名
         *
         * @return 摘要算法名
         */
        public String getAlgorithms() {
            return this.algorithms;
        }
    }
}
