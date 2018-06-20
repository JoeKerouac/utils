package com.joe.utils.poi.data;

import com.joe.utils.poi.ExcelDataWriter;
import org.apache.poi.ss.usermodel.Cell;

/**
 * @author joe
 * @version 2018.06.14 14:31
 */
public class StringDataWriter implements ExcelDataWriter<String> {
    @Override
    public void write(Cell cell, String s) {
        cell.setCellValue(s);
    }

    @Override
    public boolean writeable(Object data) {
        return data == null || (data instanceof String);
    }

    @Override
    public boolean writeable(Class<?> type) {
        if (type != null && type.equals(String.class)) {
            return true;
        }
        return false;
    }

}
