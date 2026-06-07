package com.mtfm.deadman.plugin.excel.model;

import com.mtfm.deadman.plugin.excel.column.DeadExcelColumnDefinition;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * Excel 导出选项。
 */
@Getter
@Builder
public class DeadExcelWriteOptions {

  /** 工作表名称 */
  @Builder.Default private final String sheetName = "Sheet1";

  /** 是否写入表头行 */
  @Builder.Default private final boolean includeHeader = true;

  /**
   * 自定义列定义；为 {@code null} 时从目标类型注解/字段自动解析。
   */
  private final List<DeadExcelColumnDefinition> columns;

  /** 默认导出选项 */
  public static DeadExcelWriteOptions defaults() {
    return DeadExcelWriteOptions.builder().build();
  }
}
