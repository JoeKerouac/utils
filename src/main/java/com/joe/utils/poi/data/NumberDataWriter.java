package com.joe.utils.poi.data;

import com.joe.utils.poi.ExcelDataWriter;
import com.joe.utils.type.ReflectUtil;
import org.apache.poi.ss.usermodel.Cell;

/**
 * number数据
 *
 * @author joe
 * @version 2018.06.14 11:51
 */
public final class NumberDataWriter extends ExcelDataWriter<Number> {
    @Override
    public <D extends ExcelDataWriter<Number>> D build(Object object) {
        NumberDataWriter data = new NumberDataWriter();
        data.setData(object);
        return (D) data;
    }

    @Override
    public void write(Cell cell) {
        cell.setCellValue(data.doubleValue());
    }

    @Override
    public boolean writeable(Object data) {
        return (data instanceof Number);
    }

    @Override
    public boolean writeable(Class<?> type) {
        if (type != null && (Number.class.isAssignableFrom(type) || (ReflectUtil.isGeneralType(type) && !boolean
                .class.equals(type)))) {
            return true;
        }
        return false;
    }

    @Override
    public void setData(Object data) {
        super.data = (Number) data;
    }
}
