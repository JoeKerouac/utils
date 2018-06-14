package com.joe.utils.poi;

import com.joe.utils.collection.CollectionUtil;
import com.joe.utils.common.BeanUtils;
import com.joe.utils.common.StringUtils;
import com.joe.utils.poi.data.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * POI工具类
 *
 * @author joe
 * @version 2018.06.14 11:42
 */
@Slf4j
public class POIUtils {

    /**
     * 所有的Excel单元格数据类型
     */
    private static final Map<Class, ExcelDataWriter<?>> EXCEL_DATAS = new HashMap<>();
    /**
     * 默认内存中最多多好行单元格
     */
    private static final int IN_MEMORY = 100;
    /**
     * 排序器
     */
    private static final Comparator<Field> COMPARATOR = (f1, f2) -> {
        ExcelColumn c1 = f1.getAnnotation(ExcelColumn.class);
        ExcelColumn c2 = f2.getAnnotation(ExcelColumn.class);
        if (c1 == null && c2 == null) {
            return f1.getName().compareTo(f2.getName());
        }

        if (c1 == null) {
            return 1;
        }

        if (c2 == null) {
            return -1;
        }
        return c1.sort() - c2.sort();
    };

    /**
     * 注册默认的DataWriter
     */
    static {
        EXCEL_DATAS.put(BooleanDataWriter.class, new BooleanDataWriter());
        EXCEL_DATAS.put(CalendarDataWriter.class, new CalendarDataWriter());
        EXCEL_DATAS.put(DateDataWriter.class, new DateDataWriter());
        EXCEL_DATAS.put(EnumDataWriter.class, new EnumDataWriter());
        EXCEL_DATAS.put(NumberDataWriter.class, new NumberDataWriter());
        EXCEL_DATAS.put(StringDataWriter.class, new StringDataWriter());
    }

    /**
     * 注册一个新的excel单元格DataWriter（如果原来存在那么将会覆盖原来的DataWriter）
     *
     * @param writer DataWriter
     */
    public static void registerDataWriter(ExcelDataWriter<?> writer) {
        if (writer != null) {
            EXCEL_DATAS.put(writer.getClass(), writer);
        }
    }

    /**
     * 当前系统是否包含指定DataWriter
     *
     * @param writer DataWriter
     * @return 返回true表示包含
     */
    public static boolean containsDataWriter(ExcelDataWriter<?> writer) {
        return EXCEL_DATAS.containsKey(writer.getClass());
    }

    /**
     * 注册一个新的excel单元格数据类型
     *
     * @param type 数据类型
     */
    public static void register(Class<? extends ExcelDataWriter> type) {
        if (type != null) {
            try {
                EXCEL_DATAS.put(type, type.newInstance());
            } catch (Exception e) {
                log.warn("注册类型[{}]失败", type, e);
            }
        }
    }

    /**
     * 将pojo写入本地excel
     *
     * @param datas    pojo数据
     * @param hasTitle 是否需要标题
     * @param path     excel本地路径
     * @throws IOException IO异常
     */
    public static void writeToExcel(List<? extends Object> datas, boolean hasTitle, String path) throws IOException {
        writeToExcel(datas, hasTitle, path, IN_MEMORY);
    }

    /**
     * 将pojo写入本地excel
     *
     * @param datas    pojo数据
     * @param hasTitle 是否需要标题
     * @param path     excel本地路径
     * @param inMemory 最多保留在内存中多少行
     * @throws IOException IO异常
     */
    public static void writeToExcel(List<? extends Object> datas, boolean hasTitle, String path, int inMemory) throws
            IOException {
        writeToExcel(datas, hasTitle, new FileOutputStream(path), inMemory);
    }

    /**
     * 将数据写入excel
     *
     * @param datas        要写入excel的pojo数据
     * @param hasTitle     是否需要标题
     * @param outputStream 输出流
     * @throws IOException IO异常
     */
    public static void writeToExcel(List<? extends Object> datas, boolean hasTitle, OutputStream outputStream) throws
            IOException {
        writeToExcel(datas, hasTitle, outputStream, IN_MEMORY);
    }

    /**
     * 将数据写入excel
     *
     * @param datas        要写入excel的pojo数据
     * @param hasTitle     是否需要标题
     * @param outputStream 输出流
     * @param inMemory     最多保留在内存中多少行
     * @throws IOException IO异常
     */
    public static void writeToExcel(List<? extends Object> datas, boolean hasTitle, OutputStream outputStream, int
            inMemory)
            throws IOException {
        log.info("准备将数据写入excel");
        SXSSFWorkbook wb = new SXSSFWorkbook(inMemory);
        writeToExcel(datas, hasTitle, wb);
        log.info("数据写入excel完毕，准备写入本地文件");
        wb.write(outputStream);
        outputStream.close();
        wb.dispose();
    }

    /**
     * 将pojo集合写入excel
     *
     * @param datas    pojo集合，空元素将被忽略
     * @param hasTitle 是否需要title
     * @param workbook 工作簿
     * @return 写入后的工作簿
     */
    public static Workbook writeToExcel(List<? extends Object> datas, boolean hasTitle, Workbook workbook) {
        if (CollectionUtil.safeIsEmpty(datas)) {
            log.warn("给定数据集合为空");
            return workbook;
        }
        datas = datas.parallelStream().filter(data -> data != null).collect(Collectors.toList());
        if (datas.isEmpty()) {
            log.warn("给定数据集合里的数据全是空");
            return workbook;
        }
        //获取所有字段（包括父类的）
        Field[] fields = BeanUtils.getAllFields(datas.get(0).getClass());

        log.info("获取可写入excel的字段");
        List<Field> writeFields = new ArrayList<>();
        for (Field field : fields) {
            String name = field.getName();
            log.debug("检查字段[{}]是否可以写入", name);
            Class<?> type = field.getType();

            List<ExcelDataWriter<?>> data = EXCEL_DATAS.values().stream().filter(excelData -> excelData.writeable(type))
                    .collect(Collectors.toList());
            if (data.isEmpty()) {
                log.info("字段[{}]不能写入", name);
            } else {
                log.info("字段[{}]可以写入excel");
                ExcelColumn column = field.getAnnotation(ExcelColumn.class);

                if (column == null || !column.ignore()) {
                    writeFields.add(field);
                }
            }
        }
        log.debug("可写入excel的字段集合为：[{}]", writeFields);
        log.debug("对可写入excel的字段集合排序");

        Collections.sort(writeFields, COMPARATOR);

        log.debug("可写入excel的字段集合排序完毕，构建标题列表");

        List<ExcelDataWriter<?>> titles = null;
        if (hasTitle) {
            log.info("当前需要标题列表，构建...");
            titles = new ArrayList<>(writeFields.size());
            for (int i = 0; i < writeFields.size(); i++) {
                Field field = writeFields.get(i);
                ExcelColumn column = field.getAnnotation(ExcelColumn.class);
                if (column == null || StringUtils.isEmpty(column.value())) {
                    titles.add(build(field.getName(), 0, i));
                } else {
                    titles.add(build(column.value(), 0, i));
                }
            }
        }

        log.debug("构建单元格数据");
        List<List<? extends ExcelDataWriter<?>>> writeDatas = new ArrayList<>(datas.size());
        for (int i = 0; i < datas.size(); i++) {
            Object dataValue = datas.get(i);
            //构建一行数据
            List<ExcelDataWriter<?>> columnDatas = new ArrayList<>(writeFields.size());
            //加入
            writeDatas.add(columnDatas);
            for (int j = 0; j < writeFields.size(); j++) {
                Field field = writeFields.get(j);
                try {
                    log.debug("获取[{}]中字段[{}]的值", dataValue, field.getName());
                    Object value = field.get(dataValue);
                    columnDatas.add(build(value, i, j));
                } catch (IllegalAccessException e) {
                    log.warn("[{}]中字段[{}]不能读取", dataValue, field.getName(), e);
                    columnDatas.add(null);
                }
            }
        }

        log.debug("要写入的数据为：[{}]", writeDatas);
        log.info("准备写入数据");
        writeToExcel(titles, writeDatas, hasTitle, workbook);
        log.info("标题列表为：[{}]", titles);
        return workbook;
    }

    /**
     * 写入excel
     *
     * @param titles   标题列表
     * @param datas    数据列表
     * @param hasTitle 是否需要标题
     * @param workbook 工作簿
     * @return 写入数据后的工作簿
     */
    public static Workbook writeToExcel(List<? extends ExcelDataWriter> titles, List<List<? extends
            ExcelDataWriter<?>>> datas,
                                        boolean hasTitle, Workbook workbook) {
        if (CollectionUtil.safeIsEmpty(datas)) {
            log.warn("数据为空，不写入直接返回");
            return workbook;
        }

        log.info("写入excel，{}标题", hasTitle ? "需要" : "不需要");
        Sheet sheet = workbook.createSheet();
        int rowNum = 0;

        if (hasTitle && !CollectionUtil.safeIsEmpty(titles)) {
            log.debug("需要标题，标题为：{}", titles);
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < titles.size(); i++) {
                ExcelDataWriter<?> data = titles.get(i);
                log.debug("写入第[{}]列标题：[{}]", i, data.getData());
                data.write(row.createCell(i));
            }
        }

        for (int i = rowNum; i < (rowNum + datas.size()); i++) {
            Row row = sheet.createRow(i);
            List<? extends ExcelDataWriter> columnDatas = datas.get(i - rowNum);
            if (CollectionUtil.safeIsEmpty(columnDatas)) {
                continue;
            }
            for (int j = 0; j < columnDatas.size(); j++) {
                ExcelDataWriter<?> data = columnDatas.get(j);
                if (data == null) {
                    continue;
                }
                log.debug("写入第[{}]行第[{}]列数据[{}]", i, j, data.getData());
                data.write(row.createCell(j));
            }
        }
        return workbook;
    }

    /**
     * 构建单元格数据，没有行列信息
     *
     * @param data 要写入单元格的数据
     * @return 返回不为空表示能写入，并返回单元格数据，返回空表示无法写入
     */
    public static ExcelDataWriter<?> build(Object data) {
        return build(data, 0, 0);
    }

    /**
     * 构建单元格数据
     *
     * @param data   要写入单元格的数据
     * @param row    数据所在行
     * @param column 数据所在列
     * @return 返回不为空表示能写入，并返回单元格数据，返回空表示无法写入
     */
    public static ExcelDataWriter<?> build(Object data, int row, int column) {
        List<ExcelDataWriter<?>> dataBuilder = EXCEL_DATAS.values().parallelStream().filter(excelData -> excelData
                .writeable(data)).collect(Collectors.toList());
        if (dataBuilder.isEmpty()) {
            return null;
        }
        ExcelDataWriter<?> writer = dataBuilder.get(0).build(data);
        writer.setColumn(column);
        writer.setRow(row);
        return writer;
    }
}
