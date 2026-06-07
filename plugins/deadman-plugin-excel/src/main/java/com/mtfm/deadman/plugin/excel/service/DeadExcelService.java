package com.mtfm.deadman.plugin.excel.service;

import com.mtfm.deadman.plugin.excel.model.DeadExcelReadOptions;
import com.mtfm.deadman.plugin.excel.model.DeadExcelSheetDefinition;
import com.mtfm.deadman.plugin.excel.model.DeadExcelWriteOptions;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Excel 导入导出服务。
 */
public interface DeadExcelService {

    /**
     * 从输入流导入为对象列表。
     *
     * @param input 输入流
     * @param type  目标类型（POJO 或 Record）
     * @param <T>   行类型
     * @return 解析结果
     */
    <T> List<T> importList(InputStream input, Class<T> type);

    /**
     * 按选项从输入流导入为对象列表。
     *
     * @param input   输入流
     * @param type    目标类型
     * @param options 导入选项
     * @param <T>     行类型
     * @return 解析结果
     */
    <T> List<T> importList(InputStream input, Class<T> type, DeadExcelReadOptions options);

    /**
     * 将对象列表导出到输出流。
     *
     * @param output 输出流
     * @param type   行类型
     * @param data   数据
     * @param <T>    行类型
     */
    <T> void export(OutputStream output, Class<T> type, List<T> data);

    /**
     * 按选项将对象列表导出到输出流。
     *
     * @param output  输出流
     * @param type    行类型
     * @param data    数据
     * @param options 导出选项
     * @param <T>     行类型
     */
    <T> void export(OutputStream output, Class<T> type, List<T> data, DeadExcelWriteOptions options);

    /**
     * 按工作表定义导出（支持完全自定义列）。
     *
     * @param output 输出流
     * @param sheet  工作表定义
     */
    void exportSheet(OutputStream output, DeadExcelSheetDefinition sheet);

    /**
     * 导出多个工作表到同一工作簿。
     *
     * @param output 输出流
     * @param sheets 工作表定义列表
     */
    void exportSheets(OutputStream output, List<DeadExcelSheetDefinition> sheets);
}
