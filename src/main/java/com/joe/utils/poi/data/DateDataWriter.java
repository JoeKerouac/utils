package com.joe.utils.poi.data;

import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;

import com.joe.utils.poi.ExcelDataWriter;

/**
 * 日期数据
 *
 * @author joe
 * @version 2018.06.14 11:49
 */
public final class DateDataWriter implements ExcelDataWriter<Date> {
    @Override
    public void write(Cell cell, Date data) {
        cell.setCellValue(data);
    }

    @Override
    public boolean writeable(Object data) {
        return (data instanceof Date);
    }

    @Override
    public boolean writeable(Class<?> type) {
        if (type != null && type.equals(Date.class)) {
            return true;
        }
        return false;
    }
}
