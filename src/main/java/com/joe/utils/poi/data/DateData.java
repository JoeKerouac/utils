package com.joe.utils.poi.data;

import org.apache.poi.ss.usermodel.Cell;

import java.util.Date;

/**
 * 日期数据
 *
 * @author joe
 * @version 2018.06.14 11:49
 */
public final class DateData extends ExcelData<Date> {
    @Override
    public <D extends ExcelData<Date>> D build(Object object) {
        DateData data = new DateData();
        data.setData(object);
        return (D) data;
    }

    @Override
    public void write(Cell cell) {
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

    @Override
    public void setData(Object data) {
        super.data = (Date) data;
    }
}
