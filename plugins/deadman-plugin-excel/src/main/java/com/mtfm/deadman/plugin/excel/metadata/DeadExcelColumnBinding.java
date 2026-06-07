package com.mtfm.deadman.plugin.excel.metadata;

import lombok.Builder;
import lombok.Getter;

/**
 * 类型字段与 Excel 列的绑定元数据。
 */
@Getter
@Builder(toBuilder = true)
public class DeadExcelColumnBinding {

  /** 字段名 */
  private final String fieldName;

  /** 表头名称 */
  private final String header;

  /** 列序号（从 0 开始） */
  private final int index;

  /** 列宽 */
  private final int width;

  /** 日期格式化模式 */
  private final String dateFormat;

  /** 字段类型 */
  private final Class<?> fieldType;
}
