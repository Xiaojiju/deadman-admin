package com.mtfm.deadman.plugin.excel.export;

import com.mtfm.deadman.plugin.excel.column.DeadExcelColumns;
import com.mtfm.deadman.plugin.excel.column.DeadExcelColumnDefinition;
import com.mtfm.deadman.plugin.excel.model.DeadExcelWriteOptions;
import com.mtfm.deadman.plugin.excel.service.DeadExcelService;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;

/**
 * 流式 Excel 导出构建器，便于在业务代码中链式配置列与数据。
 *
 * @param <T> 行类型
 */
@RequiredArgsConstructor
public class DeadExcelExporter<T> {

    private final DeadExcelService deadExcelService;
    private final Class<T> type;
    private List<T> data = List.of();
    private String sheetName = "Sheet1";
    private List<DeadExcelColumnDefinition> columns;

    /**
     * 创建导出构建器。
     *
     * @param deadExcelService Excel 服务
     * @param type             行类型
     * @param <T>              行类型
     * @return 构建器
     */
    public static <T> DeadExcelExporter<T> of(DeadExcelService deadExcelService, Class<T> type) {
        return new DeadExcelExporter<>(deadExcelService, type);
    }

    /**
     * 设置导出数据。
     *
     * @param data 数据列表
     * @return 当前构建器
     */
    public DeadExcelExporter<T> data(List<T> data) {
        this.data = data == null ? List.of() : List.copyOf(data);
        return this;
    }

    /**
     * 设置工作表名称。
     *
     * @param sheetName 工作表名称
     * @return 当前构建器
     */
    public DeadExcelExporter<T> sheetName(String sheetName) {
        this.sheetName = sheetName;
        return this;
    }

    /**
     * 通过列构建器自定义导出列。
     *
     * @param customizer 列构建器回调
     * @return 当前构建器
     */
    public DeadExcelExporter<T> columns(Consumer<DeadExcelColumns> customizer) {
        DeadExcelColumns builder = DeadExcelColumns.from(type);
        customizer.accept(builder);
        this.columns = builder.build();
        return this;
    }

    /**
     * 直接指定列定义。
     *
     * @param columns 列定义
     * @return 当前构建器
     */
    public DeadExcelExporter<T> columns(List<DeadExcelColumnDefinition> columns) {
        this.columns = columns;
        return this;
    }

    /**
     * 写入输出流。
     *
     * @param output 输出流
     */
    public void write(OutputStream output) {
        DeadExcelWriteOptions.DeadExcelWriteOptionsBuilder optionsBuilder =
                DeadExcelWriteOptions.builder().sheetName(sheetName);
        if (columns != null) {
            optionsBuilder.columns(columns);
        }
        deadExcelService.export(output, type, data, optionsBuilder.build());
    }
}
