package com.joe.utils.poi.data;

import com.joe.utils.poi.ExcelDataWriter;
import org.apache.poi.ss.usermodel.Cell;

/**
 * @author joe
 * @version 2018.06.14 14:31
 */
public class StringDataWriter extends ExcelDataWriter<String> {
    @Override
    public <D extends ExcelDataWriter<String>> D build(Object object) {
        StringDataWriter data = new StringDataWriter();
        data.setData(object);
        return (D) data;
    }

    @Override
    public void write(Cell cell) {
        cell.setCellValue(data);
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

    @Override
    public void setData(Object data) {
        if (data == null) {
            super.data = "null";
        } else {
            super.data = (String) data;
        }
    }
}
