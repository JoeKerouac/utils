package com.joe.utils.poi.data;

import com.joe.utils.poi.ExcelDataWriter;
import org.apache.poi.ss.usermodel.Cell;

/**
 * @author joe
 * @version 2018.06.14 11:50
 */
public final class BooleanDataWriter extends ExcelDataWriter<Boolean> {
    @Override
    public <D extends ExcelDataWriter<Boolean>> D build(Object object) {
        BooleanDataWriter data = new BooleanDataWriter();
        data.setData(object);
        return (D)data;
    }

    @Override
    public void write(Cell cell) {
        cell.setCellValue(data);
    }

    @Override
    public boolean writeable(Object data) {
        return (data instanceof Boolean);
    }

    @Override
    public boolean writeable(Class<?> type) {
        if (type == null) {
            return false;
        }
        if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return true;
        }
        return false;
    }

    @Override
    public void setData(Object data) {
        super.data = (boolean) data;
    }
}
