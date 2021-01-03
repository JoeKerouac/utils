package com.joe.utils.data;

import java.util.List;

import lombok.Data;

/**
 * @author joe
 * @version 2018.06.04 16:52
 */
@Data
public class PageData<T> {
    /**
     * 数据
     */
    private List<T> datas;
    /**
     * 当前页数，从0开始
     */
    private int currentPage;
    /**
     * 每页最多显示多少数据
     */
    private int limit;
    /**
     * 总数据量
     */
    private int total;
    /**
     * 总页数
     */
    private int totalPage;
    /**
     * 是否有下一页，true表示有下一页
     */
    private boolean hasNext;

}
