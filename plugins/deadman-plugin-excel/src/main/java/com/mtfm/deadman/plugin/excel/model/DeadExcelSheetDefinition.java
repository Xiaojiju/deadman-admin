package com.mtfm.deadman.plugin.excel.model;

import com.mtfm.deadman.plugin.excel.column.DeadExcelColumnDefinition;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * 多 Sheet 导出时的工作表定义。
 */
@Getter
@Builder
public class DeadExcelSheetDefinition {

  /** 工作表名称 */
  private final String sheetName;

  /** 列定义 */
  private final List<DeadExcelColumnDefinition> columns;

  /** 行数据，元素可为 POJO、Record 或 {@code Map} */
  private final List<?> rows;
}
