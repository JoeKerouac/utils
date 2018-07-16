package com.joe.utils.poi.data;

import org.apache.poi.ss.usermodel.Cell;

import com.joe.utils.poi.ExcelDataWriter;

/**
 * 枚举类型
 *
 * @author joe
 * @version 2018.06.14 15:10
 */
public class EnumDataWriter implements ExcelDataWriter<Enum> {
    @Override
    public void write(Cell cell, Enum data) {
        cell.setCellValue(data == null ? null : data.toString());
    }

    @Override
    public boolean writeable(Object data) {
        return (data instanceof Enum);
    }

    @Override
    public boolean writeable(Class<?> type) {
        if (type != null && type.equals(Enum.class)) {
            return true;
        }
        return false;
    }
}
