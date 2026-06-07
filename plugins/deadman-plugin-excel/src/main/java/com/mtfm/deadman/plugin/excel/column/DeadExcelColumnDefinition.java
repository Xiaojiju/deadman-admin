package com.mtfm.deadman.plugin.excel.column;

import java.util.function.Function;
import lombok.Builder;
import lombok.Getter;

/**
 * 可编程的 Excel 列定义，支持自定义表头、列宽与取值逻辑。
 */
@Getter
@Builder(toBuilder = true)
public class DeadExcelColumnDefinition {

  /** 逻辑字段名，用于从行对象取值（Map 键名或 Bean/Record 属性名） */
  private final String fieldName;

  /** 表头显示名称 */
  private final String header;

  /** 列序号（从 0 开始） */
  private final int index;

  /** 列宽（字符数） */
  @Builder.Default private final int width = 20;

  /** 日期/时间格式化模式 */
  private final String dateFormat;

  /**
   * 自定义取值函数；为 {@code null} 时按 {@link #fieldName} 从行对象读取。
   */
  private final Function<Object, Object> valueExtractor;

  /**
   * 从行对象提取单元格值。
   *
   * @param row 行数据
   * @return 单元格原始值
   */
  public Object extractValue(Object row) {
    if (valueExtractor != null) {
      return valueExtractor.apply(row);
    }
    return DeadExcelRowAccess.readProperty(row, fieldName);
  }

  /**
   * 快捷创建列定义。
   *
   * @param fieldName 字段名
   * @param header    表头
   * @return 列定义
   */
  public static DeadExcelColumnDefinition of(String fieldName, String header) {
    return DeadExcelColumnDefinition.builder().fieldName(fieldName).header(header).build();
  }

  /**
   * 快捷创建带列宽的列定义。
   *
   * @param fieldName 字段名
   * @param header    表头
   * @param width     列宽
   * @return 列定义
   */
  public static DeadExcelColumnDefinition of(String fieldName, String header, int width) {
    return DeadExcelColumnDefinition.builder()
        .fieldName(fieldName)
        .header(header)
        .width(width)
        .build();
  }
}
