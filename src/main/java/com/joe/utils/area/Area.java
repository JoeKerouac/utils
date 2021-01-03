package com.joe.utils.area;

import lombok.Data;

import java.util.List;

/**
 * 地区类
 *
 * @author JoeKerouac
 * @version 2019年10月10日 15:31
 */
@Data
public class Area {

    /**
     * 默认区域代码，当没有父地区的时候使用该代码作为父区域代码
     */
    public static final String DEFAULT = "000000";

    /**
     * 当前地区代码
     */
    private String code;

    /**
     * 父地区代码
     */
    private String parent;

    /**
     * 地区名
     */
    private String name;

    /**
     * 子地区
     */
    private List<Area> childList;

}
