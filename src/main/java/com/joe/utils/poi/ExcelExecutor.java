package com.joe.utils.poi;

import com.joe.utils.collection.CollectionUtil;
import com.joe.utils.common.BeanUtils;
import com.joe.utils.common.StringUtils;
import com.joe.utils.poi.data.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
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
 * Excel执行器，将数据写入excel，用户可以注册自己的excel单元格数据类型处理器{@link ExcelDataWriter ExcelDataWriter}来做
 * 一些特殊处理，例如添加单元格样式等，系统默认会注册Date、Calendar、String、Number、Boolean、Enum类型的数据处理器。
 *
 * @author joe
 * @version 2018.06.14 11:42
 */
@Slf4j
public class ExcelExecutor {
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
     * 默认实例
     */
    private static final ExcelExecutor UTILS = new ExcelExecutor();
    /**
     * 所有的Excel单元格数据类型
     */
    private final Map<Class<?>, ExcelDataWriter<?>> writers = new HashMap<>();

    private ExcelExecutor() {
        init();
    }

    /**
     * 初始化，注册默认的DataWriter
     */
    private void init() {
        writers.put(Boolean.class, new BooleanDataWriter());
        writers.put(Calendar.class, new CalendarDataWriter());
        writers.put(Date.class, new DateDataWriter());
        writers.put(Enum.class, new EnumDataWriter());
        writers.put(Number.class, new NumberDataWriter());
        writers.put(String.class, new StringDataWriter());
    }

    /**
     * 获取POIUtils的实例
     *
     * @return POIUtils实例，多次获取的一样
     */
    public static ExcelExecutor getInstance() {
        return UTILS;
    }

    /**
     * 构建新的POIUtils
     *
     * @return 新的POIUtils，每次返回的实例都不一样
     */
    public static ExcelExecutor buildInstance() {
        return new ExcelExecutor();
    }


    /**
     * 注册一个新的excel单元格DataWriter（如果原来存在那么将会覆盖原来的DataWriter）
     *
     * @param type   DataWriter处理的数据类型的Class
     * @param writer DataWriter
     * @param <T>    DataWriter处理的数据类型
     * @return 如果原来存在该类型的DataWriter那么返回原来的DataWriter
     */
    public <T> ExcelDataWriter<?> registerDataWriter(Class<T> type, ExcelDataWriter<T> writer) {
        if (writer != null) {
            return writers.put(type, writer);
        } else {
            return null;
        }
    }

    /**
     * 当前系统是否包含指定类型的DataWriter
     *
     * @param type DataWriter对应的数据类型的Class
     * @param <T>  DataWriter对应的数据类型
     * @return 返回true表示包含
     */
    public <T> boolean containsDataWriter(Class<T> type) {
        return writers.containsKey(type);
    }

    /**
     * 将pojo写入本地excel
     *
     * @param datas    pojo数据
     * @param hasTitle 是否需要标题
     * @param path     excel本地路径
     * @throws IOException IO异常
     */
    public void writeToExcel(List<? extends Object> datas, boolean hasTitle, String path) throws IOException {
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
    public void writeToExcel(List<? extends Object> datas, boolean hasTitle, String path, int inMemory) throws
            IOException {
        writeToExcel(datas, hasTitle, new FileOutputStream(path), inMemory);
    }

    /**
     * 将数据写入excel
     *
     * @param datas        要写入excel的pojo数据
     * @param hasTitle     是否需要标题
     * @param outputStream 输出流（该流不会关闭，需要用户手动关闭）
     * @throws IOException IO异常
     */
    public void writeToExcel(List<? extends Object> datas, boolean hasTitle, OutputStream outputStream) throws
            IOException {
        writeToExcel(datas, hasTitle, outputStream, IN_MEMORY);
    }

    /**
     * 将数据写入excel
     *
     * @param datas        要写入excel的pojo数据
     * @param hasTitle     是否需要标题
     * @param outputStream 输出流（该流不会关闭，需要用户手动关闭）
     * @param inMemory     最多保留在内存中多少行
     * @throws IOException IO异常
     */
    public void writeToExcel(List<? extends Object> datas, boolean hasTitle, OutputStream outputStream, int
            inMemory) throws IOException {
        log.info("准备将数据写入excel");
        //这里使用SXSSFWorkbook而不是XSSFWorkbook，这样将会节省内存，但是内存中仅仅存在inMemory行数据，如果超出那么会将
        //index最小的刷新到本地，后续不能通过getRow方法获取到该行
        SXSSFWorkbook wb = new SXSSFWorkbook(inMemory);
        writeToExcel(datas, hasTitle, wb);
        log.info("数据写入excel完毕，准备写入本地文件");
        wb.write(outputStream);
        log.debug("删除临时文件，关闭Workbook");
        wb.dispose();
        wb.close();
    }

    /**
     * 将pojo集合写入excel
     *
     * @param datas    pojo集合，空元素将被忽略
     * @param hasTitle 是否需要title
     * @param workbook 工作簿
     * @return 写入后的工作簿
     */
    public Workbook writeToExcel(List<? extends Object> datas, boolean hasTitle, Workbook workbook) {
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

            List<ExcelDataWriter<?>> data = writers.values().stream().filter(excelData -> excelData.writeable(type))
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

        List<Writer<?>> titles = null;
        if (hasTitle) {
            log.info("当前需要标题列表，构建...");
            titles = new ArrayList<>(writeFields.size());
            for (int i = 0; i < writeFields.size(); i++) {
                Field field = writeFields.get(i);
                ExcelColumn column = field.getAnnotation(ExcelColumn.class);
                if (column == null || StringUtils.isEmpty(column.value())) {
                    titles.add(build(field.getName()));
                } else {
                    titles.add(build(column.value()));
                }
            }
        }

        log.debug("构建单元格数据");
        List<List<? extends Writer<?>>> writeDatas = new ArrayList<>(datas.size());
        for (int i = 0; i < datas.size(); i++) {
            Object dataValue = datas.get(i);
            //构建一行数据
            List<Writer<?>> columnDatas = new ArrayList<>(writeFields.size());
            //加入
            writeDatas.add(columnDatas);
            for (int j = 0; j < writeFields.size(); j++) {
                Field field = writeFields.get(j);
                try {
                    log.debug("获取[{}]中字段[{}]的值", dataValue, field.getName());
                    Object value = field.get(dataValue);
                    columnDatas.add(build(value));
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
     * 写入excel（实际处理方法，在该方法中数据将会被写入excel）
     *
     * @param titles   标题列表
     * @param datas    数据列表
     * @param hasTitle 是否需要标题
     * @param workbook 工作簿
     * @return 写入数据后的工作簿
     */
    private Workbook writeToExcel(List<? extends Writer<?>> titles, List<List<? extends Writer<?>>> datas, boolean
            hasTitle, Workbook workbook) {
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
                Writer<?> data = titles.get(i);
                log.debug("写入第[{}]列标题：[{}]", i, data.data);
                data.write(row.createCell(i));
            }
        }

        for (int i = rowNum; i < (rowNum + datas.size()); i++) {
            Row row = sheet.createRow(i);
            List<? extends Writer> columnDatas = datas.get(i - rowNum);
            if (CollectionUtil.safeIsEmpty(columnDatas)) {
                continue;
            }
            for (int j = 0; j < columnDatas.size(); j++) {
                Writer<?> data = columnDatas.get(j);
                if (data == null) {
                    continue;
                }
                log.debug("写入第[{}]行第[{}]列数据[{}]", i, j, data.data);
                data.write(row.createCell(j));
            }
        }
        return workbook;
    }

    /**
     * 构建单元格数据
     *
     * @param data 要写入单元格的数据
     * @return 返回不为空表示能写入，并返回单元格数据，返回空表示无法写入
     */
    private Writer<?> build(Object data) {
        List<ExcelDataWriter<?>> dataBuilder = writers.values().parallelStream().filter(excelData -> excelData
                .writeable(data)).collect(Collectors.toList());
        if (dataBuilder.isEmpty()) {
            return null;
        } else {
            return new Writer(dataBuilder.get(0), data);
        }
    }

    @AllArgsConstructor
    private class Writer<T> {
        private final ExcelDataWriter<T> writer;
        private final T data;

        public void write(Cell cell) {
            writer.write(cell, data);
        }
    }
}
