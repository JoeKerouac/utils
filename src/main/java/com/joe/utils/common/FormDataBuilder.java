package com.joe.utils.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * form数据构建器（非线程安全）
 *
 * @author joe
 * @version 2018.05.14 11:15
 */
public class FormDataBuilder {
    private Map<String, Object> datas;

    private FormDataBuilder(boolean sort) {
        this(sort, null);
    }

    private FormDataBuilder(boolean sort, Map<String, ?> data) {
        if (sort) {
            this.datas = new TreeMap<>();
        } else {
            this.datas = new HashMap<>();
        }
        if (data != null && !data.isEmpty()) {
            this.datas.putAll(data);
        }
    }

    /**
     * 添加form数据（key不能重复）
     *
     * @param key   key
     * @param value value
     * @return FormDataBuilder
     */
    public FormDataBuilder form(String key, Object value) {
        datas.put(key, value == null ? "" : value);
        return this;
    }

    /**
     * 删除一个form数据
     *
     * @param key 要删除的key
     * @return FormDataBuilder
     */
    public FormDataBuilder del(String key) {
        datas.remove(key);
        return this;
    }

    /**
     * 获取form数据
     *
     * @return form数据
     */
    public String data() {
        StringBuilder sb = new StringBuilder();
        datas.forEach((k, v) -> sb.append("&").append(k).append("=").append(String.valueOf(v)));
        return sb.toString().substring(1);
    }

    /**
     * 获取form数据
     *
     * @param useUrlencode 是否使用URLEncode对value进行编码，true表示使用URLEncode进行编码
     * @param charset      编码字符集
     * @return form数据
     */
    public String data(boolean useUrlencode, String charset) {
        StringBuilder sb = new StringBuilder();
        if (useUrlencode) {
            datas.forEach((k, v) -> sb.append("&")
                    .append(k)
                    .append("=")
                    .append(urlencode(String.valueOf(v), charset)));
        } else {
            datas.forEach((k, v) -> sb.append("&")
                    .append(k)
                    .append("=")
                    .append(String.valueOf(v)));
        }
        return sb.toString().substring(1);
    }

    /**
     * 对数据进行URLEncode编码
     *
     * @param data    数据
     * @param charset 字符集
     * @return 编码后的数据
     */
    private String urlencode(String data, String charset) {
        try {
            return URLEncoder.encode(String.valueOf(data), charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 构建一个FormDataBuilder（不排序）
     *
     * @return FormDataBuilder
     */
    public static FormDataBuilder builder() {
        return builder(false, null);
    }

    /**
     * 构建一个FormDataBuilder（不排序）
     *
     * @param data 预设data
     * @return FormDataBuilder
     */
    public static FormDataBuilder builder(Map<String, ?> data) {
        return new FormDataBuilder(false, data);
    }

    /**
     * 构建一个FormDataBuilder
     *
     * @param sort 是否排序（默认按照字典序排）
     * @return FormDataBuilder
     */
    public static FormDataBuilder builder(boolean sort) {
        return builder(sort, null);
    }

    /**
     * 构建一个FormDataBuilder
     *
     * @param sort 是否排序（默认按照字典序排）
     * @param data 预设data
     * @return FormDataBuilder
     */
    public static FormDataBuilder builder(boolean sort, Map<String, ?> data) {
        return new FormDataBuilder(sort, data);
    }
}
