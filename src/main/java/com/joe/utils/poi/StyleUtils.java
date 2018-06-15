package com.joe.utils.poi;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * 设置单元格样式
 *
 * @author joe
 * @version 2018.06.15 16:22
 */
public class StyleUtils {
    private Cell cell;
    private Workbook workbook;
    private CellStyle style;

    private StyleUtils(Cell cell) {
        this.cell = cell;
        this.workbook = cell.getSheet().getWorkbook();
        this.style = cell.getCellStyle();
    }

    public static StyleUtils build(Cell cell) {
        return new StyleUtils(cell);
    }

    public StyleUtils setFontSize(short size) {
        Font font = workbook.createFont();
        font.setFontHeightInPoints(size);
        return this;
    }
}
