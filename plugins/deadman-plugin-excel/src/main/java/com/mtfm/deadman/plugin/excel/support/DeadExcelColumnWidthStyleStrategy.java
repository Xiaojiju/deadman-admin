package com.mtfm.deadman.plugin.excel.support;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.style.column.AbstractColumnWidthStyleStrategy;
import com.mtfm.deadman.plugin.excel.column.DeadExcelColumnDefinition;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Cell;

/**
 * 按列定义设置 Excel 列宽的写入策略。
 */
public class DeadExcelColumnWidthStyleStrategy extends AbstractColumnWidthStyleStrategy {

  private final Map<Integer, Integer> widthMap;

  /**
   * 构造列宽策略。
   *
   * @param columns 列定义
   */
  public DeadExcelColumnWidthStyleStrategy(List<DeadExcelColumnDefinition> columns) {
    this.widthMap =
        columns.stream()
            .collect(Collectors.toMap(DeadExcelColumnDefinition::getIndex, DeadExcelColumnDefinition::getWidth));
  }

  /**
   * 设置列宽。
   *
   * @param writeSheetHolder 工作表上下文
   * @param cellDataList     单元格数据
   * @param cell             当前单元格
   * @param head             表头信息
   * @param relativeRowIndex 相对行号
   * @param isHead           是否表头行
   */
  @Override
  protected void setColumnWidth(
      WriteSheetHolder writeSheetHolder,
      List<WriteCellData<?>> cellDataList,
      Cell cell,
      Head head,
      Integer relativeRowIndex,
      Boolean isHead) {
    Integer width = widthMap.get(cell.getColumnIndex());
    if (width == null) {
      return;
    }
    writeSheetHolder.getSheet().setColumnWidth(cell.getColumnIndex(), width * 256);
  }
}
