package com.joe.utils.poi;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * 单元格样式访问器
 *
 * @author joe
 * @version 2018.06.15 16:22
 */
public class CellStyleAccesser {
    private Cell      cell;
    private Workbook  workbook;
    private CellStyle style;
    private Font      font;

    private CellStyleAccesser(Cell cell) {
        this.cell = cell;
        workbook = cell.getSheet().getWorkbook();
        style = cell.getCellStyle();
        //如果原来有style就用默认的
        if (style.getIndex() == 0) {
            //原来没有style，创建一个
            style = workbook.createCellStyle();
        }
        cell.setCellStyle(style);
        //原来有font就用原来的
        if (style.getFontIndex() == 0) {
            //原来没有设置font
            font = workbook.createFont();
        } else {
            //设置过font
            font = workbook.getFontAt(style.getFontIndex());
        }
        style.setFont(font);
    }

    /**
     * 构建单元格样式访问器，用来修改单元格样式
     *
     * @param cell 单元格
     * @return CellStyleAccesser
     */
    public static CellStyleAccesser build(Cell cell) {
        return new CellStyleAccesser(cell);
    }

    /**
     * 设置字号
     *
     * @param size 字号
     * @return CellStyleAccesser
     */
    public CellStyleAccesser setFontSize(short size) {
        font.setFontHeightInPoints(size);
        return this;
    }

    /**
     * 设置粗体
     *
     * @param bold 是否设置粗体，true表示设置粗体
     * @return CellStyleAccesser
     */
    public CellStyleAccesser bold(boolean bold) {
        font.setBold(bold);
        return this;
    }

    /**
     * 设置单元格字体颜色
     *
     * @param color 颜色
     * @return CellStyleAccesser
     */
    public CellStyleAccesser color(HSSFColor.HSSFColorPredefined color) {
        font.setColor(color.getIndex());
        return this;
    }
}
