package com.mtfm.deadman.plugin.excel.support;

import com.mtfm.deadman.plugin.excel.exception.DeadExcelException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Excel 单元格文本与 Java 类型之间的转换工具。
 */
public final class DeadExcelCellConverter {

  private DeadExcelCellConverter() {}

  /**
   * 将单元格文本转换为目标类型。
   *
   * @param text       单元格文本
   * @param targetType 目标类型
   * @param dateFormat 日期格式（可空）
   * @return 转换后的值
   */
  public static Object convert(String text, Class<?> targetType, String dateFormat) {
    if (text == null || text.isBlank()) {
      return null;
    }
    if (targetType == String.class) {
      return text.trim();
    }
    if (targetType == Integer.class || targetType == int.class) {
      return Integer.valueOf(text.trim());
    }
    if (targetType == Long.class || targetType == long.class) {
      return Long.valueOf(text.trim());
    }
    if (targetType == Boolean.class || targetType == boolean.class) {
      return parseBoolean(text.trim());
    }
    if (targetType == BigDecimal.class) {
      return new BigDecimal(text.trim());
    }
    if (targetType == LocalDate.class) {
      return parseLocalDate(text.trim(), dateFormat);
    }
    if (targetType == LocalDateTime.class) {
      return parseLocalDateTime(text.trim(), dateFormat);
    }
    if (targetType.isEnum()) {
      return parseEnumValue(targetType, text.trim());
    }
    throw new DeadExcelException("不支持的字段类型：" + targetType.getName());
  }

  /**
   * 将 Java 值格式化为可写入 Excel 的展示值。
   *
   * @param value      原始值
   * @param dateFormat 日期格式（可空）
   * @return 展示值
   */
  public static Object formatForWrite(Object value, String dateFormat) {
    if (value == null) {
      return null;
    }
    if (value instanceof LocalDateTime dateTime) {
      DateTimeFormatter formatter =
          DateTimeFormatter.ofPattern(dateFormat == null ? "yyyy-MM-dd HH:mm:ss" : dateFormat);
      return formatter.format(dateTime);
    }
    if (value instanceof LocalDate date) {
      DateTimeFormatter formatter =
          DateTimeFormatter.ofPattern(dateFormat == null ? "yyyy-MM-dd" : dateFormat);
      return formatter.format(date);
    }
    if (value instanceof Enum<?> enumValue) {
      return enumValue.name();
    }
    return value;
  }

  private static boolean parseBoolean(String text) {
    return "true".equalsIgnoreCase(text)
        || "1".equals(text)
        || "是".equals(text)
        || "Y".equalsIgnoreCase(text);
  }

  private static LocalDate parseLocalDate(String text, String dateFormat) {
    if (dateFormat != null && !dateFormat.isBlank()) {
      return LocalDate.parse(text, DateTimeFormatter.ofPattern(dateFormat));
    }
    try {
      return LocalDate.parse(text);
    } catch (DateTimeParseException ex) {
      return LocalDateTime.parse(text).toLocalDate();
    }
  }

  private static LocalDateTime parseLocalDateTime(String text, String dateFormat) {
    if (dateFormat != null && !dateFormat.isBlank()) {
      return LocalDateTime.parse(text, DateTimeFormatter.ofPattern(dateFormat));
    }
    return LocalDateTime.parse(text.replace(' ', 'T'));
  }

  @SuppressWarnings("unchecked")
  private static <E extends Enum<E>> E parseEnumValue(Class<?> targetType, String text) {
    Class<E> enumType = (Class<E>) targetType;
    return Enum.valueOf(enumType, text);
  }
}
