package com.joe.utils.poi.data;

import org.apache.poi.ss.usermodel.Cell;

import java.util.Calendar;

/**
 * @author joe
 * @version 2018.06.14 11:59
 */
public final class CalendarData extends ExcelData<Calendar> {
    @Override
    public <D extends ExcelData<Calendar>> D build(Object object) {
        CalendarData data = new CalendarData();
        data.setData(object);
        return (D) data;
    }

    @Override
    public void write(Cell cell) {
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

    @Override
    public void setData(Object data) {
        super.data = (Calendar) data;
    }
}
