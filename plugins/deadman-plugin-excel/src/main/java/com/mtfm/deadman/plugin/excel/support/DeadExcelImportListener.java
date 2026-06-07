package com.mtfm.deadman.plugin.excel.support;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.mtfm.deadman.plugin.excel.metadata.DeadExcelMetadata;
import com.mtfm.deadman.plugin.excel.model.DeadExcelReadOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 将 EasyExcel 行数据映射为目标类型的读取监听器。
 *
 * @param <T> 行类型
 */
public class DeadExcelImportListener<T> extends AnalysisEventListener<Map<Integer, String>> {

  private final DeadExcelReadOptions options;
  private final DeadExcelMetadata metadata;
  private final List<T> rows = new ArrayList<>();
  private final Map<String, Integer> headerIndex = new HashMap<>();

  /**
   * 构造导入监听器。
   *
   * @param options  导入选项
   * @param metadata 类型元数据
   */
  public DeadExcelImportListener(DeadExcelReadOptions options, DeadExcelMetadata metadata) {
    this.options = options;
    this.metadata = metadata;
  }

  /**
   * 解析表头行，建立表头名称与列序号的映射。
   *
   * @param headMap 表头映射
   * @param context 解析上下文
   */
  @Override
  public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
    headMap.forEach(
        (index, header) -> {
          if (header != null && !header.isBlank()) {
            headerIndex.put(header.trim(), index);
          }
        });
  }

  /**
   * 解析数据行并转换为目标类型。
   *
   * @param data    行数据
   * @param context 解析上下文
   */
  @Override
  @SuppressWarnings("unchecked")
  public void invoke(Map<Integer, String> data, AnalysisContext context) {
    if (options.getMaxRows() != null && rows.size() >= options.getMaxRows()) {
      return;
    }
    Object instance = metadata.createInstance(data, headerIndex, options.isMatchByHeader());
    rows.add((T) instance);
  }

  /**
   * 全部行解析完成后的回调。
   *
   * @param context 解析上下文
   */
  @Override
  public void doAfterAllAnalysed(AnalysisContext context) {
    // 无需额外处理
  }

  /**
   * 获取已解析的数据行。
   *
   * @return 数据列表
   */
  public List<T> getRows() {
    return List.copyOf(rows);
  }
}
