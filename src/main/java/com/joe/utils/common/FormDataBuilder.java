package com.joe.utils.common;

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
    private Map<String, String> datas;

    private FormDataBuilder(boolean sort) {
        this(sort, null);
    }

    private FormDataBuilder(boolean sort, Map<String, String> data) {
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
    public FormDataBuilder form(String key, String value) {
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
        datas.forEach((k, v) -> sb.append("&").append(k).append("=").append(v));
        return sb.toString().substring(1);
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
    public static FormDataBuilder builder(Map<String, String> data) {
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
    public static FormDataBuilder builder(boolean sort, Map<String, String> data) {
        return new FormDataBuilder(sort, data);
    }
}
