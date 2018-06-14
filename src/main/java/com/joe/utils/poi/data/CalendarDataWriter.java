package com.joe.utils.poi.data;

import com.joe.utils.poi.ExcelDataWriter;
import org.apache.poi.ss.usermodel.Cell;

import java.util.Calendar;

/**
 * @author joe
 * @version 2018.06.14 11:59
 */
public final class CalendarDataWriter extends ExcelDataWriter<Calendar> {
    @Override
    public <D extends ExcelDataWriter<Calendar>> D build(Object object) {
        CalendarDataWriter data = new CalendarDataWriter();
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
