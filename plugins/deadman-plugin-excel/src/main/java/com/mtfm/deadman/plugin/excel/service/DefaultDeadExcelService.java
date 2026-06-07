package com.mtfm.deadman.plugin.excel.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.mtfm.deadman.plugin.excel.column.DeadExcelColumnDefinition;
import com.mtfm.deadman.plugin.excel.exception.DeadExcelException;
import com.mtfm.deadman.plugin.excel.metadata.DeadExcelMetadata;
import com.mtfm.deadman.plugin.excel.model.DeadExcelReadOptions;
import com.mtfm.deadman.plugin.excel.model.DeadExcelSheetDefinition;
import com.mtfm.deadman.plugin.excel.model.DeadExcelWriteOptions;
import com.mtfm.deadman.plugin.excel.support.DeadExcelCellConverter;
import com.mtfm.deadman.plugin.excel.support.DeadExcelColumnWidthStyleStrategy;
import com.mtfm.deadman.plugin.excel.support.DeadExcelImportListener;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于 EasyExcel 的默认 Excel 导入导出实现。
 */
@Slf4j
public class DefaultDeadExcelService implements DeadExcelService {

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<T> importList(InputStream input, Class<T> type) {
        return importList(input, type, DeadExcelReadOptions.defaults());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<T> importList(InputStream input, Class<T> type, DeadExcelReadOptions options) {
        DeadExcelMetadata metadata = DeadExcelMetadata.of(type);
        DeadExcelImportListener<T> listener = new DeadExcelImportListener<>(options, metadata);
        try {
            EasyExcel.read(input)
                    .registerReadListener(listener)
                    .sheet(options.getSheetIndex())
                    .headRowNumber(options.getHeadRowNumber())
                    .doRead();
            return listener.getRows();
        } catch (Exception ex) {
            throw new DeadExcelException("Excel 导入失败：" + type.getSimpleName(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> void export(OutputStream output, Class<T> type, List<T> data) {
        export(output, type, data, DeadExcelWriteOptions.defaults());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> void export(OutputStream output, Class<T> type, List<T> data, DeadExcelWriteOptions options) {
        List<DeadExcelColumnDefinition> columns = resolveColumns(type, options);
        DeadExcelSheetDefinition sheet =
                DeadExcelSheetDefinition.builder()
                        .sheetName(options.getSheetName())
                        .columns(columns)
                        .rows(data)
                        .build();
        exportSheet(output, sheet, options.isIncludeHeader());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportSheet(OutputStream output, DeadExcelSheetDefinition sheet) {
        exportSheet(output, sheet, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportSheets(OutputStream output, List<DeadExcelSheetDefinition> sheets) {
        if (sheets == null || sheets.isEmpty()) {
            throw new DeadExcelException("导出工作表列表不能为空");
        }
        try (ExcelWriter writer = EasyExcel.write(output).build()) {
            for (int i = 0; i < sheets.size(); i++) {
                DeadExcelSheetDefinition sheet = sheets.get(i);
                validateSheet(sheet);
                List<DeadExcelColumnDefinition> columns = sortedColumns(sheet.getColumns());
                WriteSheet writeSheet =
                        EasyExcel.writerSheet(i, sheet.getSheetName())
                                .head(buildHead(columns))
                                .registerWriteHandler(new DeadExcelColumnWidthStyleStrategy(columns))
                                .build();
                writer.write(buildRows(columns, sheet.getRows()), writeSheet);
            }
        } catch (DeadExcelException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DeadExcelException("Excel 多 Sheet 导出失败", ex);
        }
    }

  private void exportSheet(OutputStream output, DeadExcelSheetDefinition sheet, boolean includeHeader) {
        validateSheet(sheet);
        List<DeadExcelColumnDefinition> columns = sortedColumns(sheet.getColumns());
        List<List<String>> head = includeHeader ? buildHead(columns) : List.of();
        List<List<Object>> rows = buildRows(columns, sheet.getRows());
        try {
            var writer = EasyExcel.write(output).registerWriteHandler(new DeadExcelColumnWidthStyleStrategy(columns));
            if (includeHeader) {
                writer.head(head);
            }
            writer.sheet(sheet.getSheetName()).doWrite(rows);
        } catch (Exception ex) {
            throw new DeadExcelException("Excel 导出失败", ex);
        }
    }

    private static void validateSheet(DeadExcelSheetDefinition sheet) {
        if (sheet.getColumns() == null || sheet.getColumns().isEmpty()) {
            throw new DeadExcelException("导出列定义不能为空");
        }
        if (sheet.getRows() == null) {
            throw new DeadExcelException("导出行数据不能为 null");
        }
    }

    private static List<DeadExcelColumnDefinition> resolveColumns(Class<?> type, DeadExcelWriteOptions options) {
        if (options.getColumns() != null && !options.getColumns().isEmpty()) {
            return sortedColumns(options.getColumns());
        }
        return sortedColumns(DeadExcelMetadata.of(type).toColumnDefinitions());
    }

    private static List<DeadExcelColumnDefinition> sortedColumns(List<DeadExcelColumnDefinition> columns) {
        List<DeadExcelColumnDefinition> sorted = new ArrayList<>(columns);
        sorted.sort(Comparator.comparingInt(DeadExcelColumnDefinition::getIndex));
        return List.copyOf(sorted);
    }

    private static List<List<String>> buildHead(List<DeadExcelColumnDefinition> columns) {
        return columns.stream().map(column -> List.of(column.getHeader())).toList();
    }

    private static List<List<Object>> buildRows(List<DeadExcelColumnDefinition> columns, List<?> rows) {
        return rows.stream()
                .map(
                        row ->
                                columns.stream()
                                        .map(
                                                column -> {
                                                    Object raw = column.extractValue(row);
                                                    return DeadExcelCellConverter.formatForWrite(
                                                            raw, column.getDateFormat());
                                                })
                                        .toList())
                .toList();
    }
}
