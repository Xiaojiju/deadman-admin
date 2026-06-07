package com.mtfm.deadman.plugin.excel.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Excel 导入选项。
 */
@Getter
@Builder
public class DeadExcelReadOptions {

  /** 工作表序号（从 0 开始） */
  @Builder.Default private final int sheetIndex = 0;

  /** 表头所在行号（从 1 开始，与 EasyExcel 一致） */
  @Builder.Default private final int headRowNumber = 1;

  /** 最大读取行数（不含表头），{@code null} 表示不限制 */
  private final Integer maxRows;

  /**
   * 是否按表头名称匹配列；为 {@code false} 时仅按列序号映射。
   */
  @Builder.Default private final boolean matchByHeader = true;

  /** 默认导入选项 */
  public static DeadExcelReadOptions defaults() {
    return DeadExcelReadOptions.builder().build();
  }
}
