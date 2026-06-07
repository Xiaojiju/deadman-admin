package com.mtfm.deadman.plugin.excel.column;

import com.mtfm.deadman.plugin.excel.metadata.DeadExcelMetadata;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 自定义列构建器，支持基于类型元数据裁剪、重命名、追加计算列与重排列顺序。
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DeadExcelColumns {

  private final Map<String, DeadExcelColumnDefinition> columnMap = new LinkedHashMap<>();
  private List<String> columnOrder;

  /**
   * 基于类型注解/字段创建列构建器。
   *
   * @param type 行类型
   * @return 列构建器
   */
  public static DeadExcelColumns from(Class<?> type) {
    DeadExcelColumns builder = new DeadExcelColumns();
    for (DeadExcelColumnDefinition column : DeadExcelMetadata.of(type).toColumnDefinitions()) {
      builder.columnMap.put(column.getFieldName(), column);
    }
    return builder;
  }

  /**
   * 创建空列构建器。
   *
   * @return 列构建器
   */
  public static DeadExcelColumns create() {
    return new DeadExcelColumns();
  }

  /**
   * 排除指定字段列。
   *
   * @param fieldNames 字段名
   * @return 当前构建器
   */
  public DeadExcelColumns exclude(String... fieldNames) {
    Arrays.stream(fieldNames).forEach(columnMap::remove);
    return this;
  }

  /**
   * 仅保留指定字段列。
   *
   * @param fieldNames 字段名
   * @return 当前构建器
   */
  public DeadExcelColumns only(String... fieldNames) {
    Map<String, DeadExcelColumnDefinition> filtered = new LinkedHashMap<>();
    for (String fieldName : fieldNames) {
      DeadExcelColumnDefinition column = columnMap.get(fieldName);
      if (column != null) {
        filtered.put(fieldName, column);
      }
    }
    columnMap.clear();
    columnMap.putAll(filtered);
    columnOrder = List.of(fieldNames);
    return this;
  }

  /**
   * 修改已有列的表头名称。
   *
   * @param fieldName 字段名
   * @param header    新表头
   * @return 当前构建器
   */
  public DeadExcelColumns header(String fieldName, String header) {
    DeadExcelColumnDefinition existing = columnMap.get(fieldName);
    if (existing == null) {
      putColumn(fieldName, header, 20, null);
      return this;
    }
    columnMap.put(
        fieldName,
        existing.toBuilder().header(header).build());
    return this;
  }

  /**
   * 修改已有列的列宽。
   *
   * @param fieldName 字段名
   * @param width     列宽
   * @return 当前构建器
   */
  public DeadExcelColumns width(String fieldName, int width) {
    DeadExcelColumnDefinition existing = requireColumn(fieldName);
    columnMap.put(fieldName, existing.toBuilder().width(width).build());
    return this;
  }

  /**
   * 为已有列设置自定义取值函数。
   *
   * @param fieldName 字段名
   * @param extractor 取值函数
   * @return 当前构建器
   */
  public DeadExcelColumns value(String fieldName, Function<Object, Object> extractor) {
    DeadExcelColumnDefinition existing = requireColumn(fieldName);
    columnMap.put(fieldName, existing.toBuilder().valueExtractor(extractor).build());
    return this;
  }

  /**
   * 新增或覆盖一列。
   *
   * @param fieldName 字段名
   * @param header    表头
   * @return 当前构建器
   */
  public DeadExcelColumns column(String fieldName, String header) {
    return putColumn(fieldName, header, 20, null);
  }

  /**
   * 新增或覆盖一列（含列宽）。
   *
   * @param fieldName 字段名
   * @param header    表头
   * @param width     列宽
   * @return 当前构建器
   */
  public DeadExcelColumns column(String fieldName, String header, int width) {
    return putColumn(fieldName, header, width, null);
  }

  /**
   * 新增计算列（通过取值函数生成单元格值）。
   *
   * @param fieldName 逻辑字段名
   * @param header    表头
   * @param extractor 取值函数
   * @return 当前构建器
   */
  public DeadExcelColumns column(String fieldName, String header, Function<Object, Object> extractor) {
    return putColumn(fieldName, header, 20, extractor);
  }

  /**
   * 重排列顺序。
   *
   * @param fieldNames 字段顺序
   * @return 当前构建器
   */
  public DeadExcelColumns reorder(String... fieldNames) {
    this.columnOrder = List.of(fieldNames);
    return this;
  }

  /**
   * 构建列定义列表（按 index 排序）。
   *
   * @return 列定义
   */
  public List<DeadExcelColumnDefinition> build() {
    List<DeadExcelColumnDefinition> columns = new ArrayList<>();
    if (columnOrder != null) {
      int index = 0;
      for (String fieldName : columnOrder) {
        DeadExcelColumnDefinition column = columnMap.remove(fieldName);
        if (column != null) {
          columns.add(reindex(column, index++));
        }
      }
      int nextIndex = index;
      for (DeadExcelColumnDefinition column : columnMap.values()) {
        columns.add(reindex(column, nextIndex++));
      }
    } else {
      columns.addAll(columnMap.values());
      columns.sort((left, right) -> Integer.compare(left.getIndex(), right.getIndex()));
      for (int i = 0; i < columns.size(); i++) {
        columns.set(i, reindex(columns.get(i), i));
      }
    }
    return List.copyOf(columns);
  }

  private DeadExcelColumnDefinition requireColumn(String fieldName) {
    DeadExcelColumnDefinition existing = columnMap.get(fieldName);
    if (existing == null) {
      throw new IllegalArgumentException("列不存在：" + fieldName);
    }
    return existing;
  }

  private DeadExcelColumns putColumn(
      String fieldName, String header, int width, Function<Object, Object> extractor) {
    int index = columnMap.size();
    columnMap.put(
        fieldName,
        DeadExcelColumnDefinition.builder()
            .fieldName(fieldName)
            .header(header)
            .index(index)
            .width(width)
            .valueExtractor(extractor)
            .build());
    return this;
  }

  private static DeadExcelColumnDefinition reindex(DeadExcelColumnDefinition column, int index) {
    if (column.getIndex() == index) {
      return column;
    }
    return column.toBuilder().index(index).build();
  }
}
