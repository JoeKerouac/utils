package com.joe.utils.poi.data;

import java.util.Calendar;

import org.apache.poi.ss.usermodel.Cell;

import com.joe.utils.poi.ExcelDataWriter;

/**
 * @author joe
 * @version 2018.06.14 11:59
 */
public final class CalendarDataWriter implements ExcelDataWriter<Calendar> {
    @Override
    public void write(Cell cell, Calendar data) {
        cell.setCellValue(data);
    }

    @Override
    public boolean writeable(Object data) {
        return (data instanceof Calendar);
    }

    @Override
    public boolean writeable(Class<?> type) {
        if (type != null && type.equals(Calendar.class)) {
            return true;
        }
        return false;
    }

}
