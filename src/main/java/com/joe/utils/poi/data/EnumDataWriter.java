package com.joe.utils.poi.data;

import com.joe.utils.poi.ExcelDataWriter;
import org.apache.poi.ss.usermodel.Cell;

/**
 * 枚举类型
 *
 * @author joe
 * @version 2018.06.14 15:10
 */
public class EnumDataWriter extends ExcelDataWriter<Enum> {
    @Override
    public <D extends ExcelDataWriter<Enum>> D build(Object object) {
        EnumDataWriter data = new EnumDataWriter();
        data.setData(object);
        return (D) data;
    }

    @Override
    public void write(Cell cell) {
        cell.setCellValue(data == null ? "" : data.toString());
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

    @Override
    public void setData(Object data) {
        super.data = (Enum) data;
    }
}
