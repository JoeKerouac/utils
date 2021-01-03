package com.joe.utils.poi.data;

import org.apache.poi.ss.usermodel.Cell;

import com.joe.utils.poi.ExcelDataWriter;
import com.joe.utils.reflect.type.JavaTypeUtil;

/**
 * number数据
 *
 * @author joe
 * @version 2018.06.14 11:51
 */
public final class NumberDataWriter implements ExcelDataWriter<Number> {
    @Override
    public void write(Cell cell, Number data) {
        cell.setCellValue(data == null ? null : data.doubleValue());
    }

    @Override
    public boolean writeable(Object data) {
        return (data instanceof Number);
    }

    @Override
    public boolean writeable(Class<?> type) {
        if (type != null && (Number.class.isAssignableFrom(type)
            || (JavaTypeUtil.isGeneralType(type) && !boolean.class.equals(type)))) {
            return true;
        }
        return false;
    }
}
