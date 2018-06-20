package com.joe.utils.poi;

import org.apache.poi.ss.usermodel.Cell;

/**
 * excel单元格的DataWriter，用于往单元格写入数据，可以自己定制，例如要加入样式的时候可以自己定制
 *
 * @author joe
 * @version 2018.06.14 11:46
 */
public interface ExcelDataWriter<T> {
    /**
     * 将数据写入单元格
     *
     * @param cell 单元格
     * @param data 要写入的数据
     */
    void write(Cell cell, T data);

    /**
     * 数据是否可写
     *
     * @param data 要写入的数据
     * @return 返回true表示可写
     */
    boolean writeable(Object data);

    /**
     * 数据类型是否可写
     *
     * @param type 数据类型
     * @return 返回true表示可写
     */
    boolean writeable(Class<?> type);
}
