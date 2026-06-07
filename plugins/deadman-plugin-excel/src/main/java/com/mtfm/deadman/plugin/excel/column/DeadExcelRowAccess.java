package com.mtfm.deadman.plugin.excel.column;

import com.mtfm.deadman.plugin.excel.exception.DeadExcelException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Map;

/**
 * 从 POJO、Record 或 Map 读取/写入属性值的反射工具。
 */
final class DeadExcelRowAccess {

  private DeadExcelRowAccess() {}

  /**
   * 读取行对象属性值。
   *
   * @param row       行对象
   * @param fieldName 属性名
   * @return 属性值，不存在时返回 {@code null}
   */
  static Object readProperty(Object row, String fieldName) {
    if (row == null || fieldName == null) {
      return null;
    }
    if (row instanceof Map<?, ?> map) {
      return map.get(fieldName);
    }
    Class<?> type = row.getClass();
    if (type.isRecord()) {
      return readRecordComponent(row, type, fieldName);
    }
    return readPojoField(row, type, fieldName);
  }

  /**
   * 向行对象写入属性值（仅支持 POJO 字段）。
   *
   * @param row       行对象
   * @param fieldName 属性名
   * @param value     属性值
   */
  static void writeProperty(Object row, String fieldName, Object value) {
    if (row == null || fieldName == null) {
      return;
    }
    if (row instanceof Map<?, ?> map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> writable = (Map<String, Object>) map;
      writable.put(fieldName, value);
      return;
    }
    Class<?> type = row.getClass();
    if (type.isRecord()) {
      throw new DeadExcelException("Record 类型不支持可变写入：" + type.getName());
    }
    writePojoField(row, type, fieldName, value);
  }

  private static Object readRecordComponent(Object row, Class<?> type, String fieldName) {
    for (RecordComponent component : type.getRecordComponents()) {
      if (component.getName().equals(fieldName)) {
        try {
          Method accessor = component.getAccessor();
          accessor.setAccessible(true);
          return accessor.invoke(row);
        } catch (ReflectiveOperationException ex) {
          throw new DeadExcelException("读取 Record 组件失败：" + fieldName, ex);
        }
      }
    }
    return null;
  }

  private static Object readPojoField(Object row, Class<?> type, String fieldName) {
    Class<?> current = type;
    while (current != null && current != Object.class) {
      try {
        Field field = current.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(row);
      } catch (NoSuchFieldException ignored) {
        current = current.getSuperclass();
      } catch (IllegalAccessException ex) {
        throw new DeadExcelException("读取字段失败：" + fieldName, ex);
      }
    }
    return null;
  }

  private static void writePojoField(Object row, Class<?> type, String fieldName, Object value) {
    Class<?> current = type;
    while (current != null && current != Object.class) {
      try {
        Field field = current.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(row, value);
        return;
      } catch (NoSuchFieldException ignored) {
        current = current.getSuperclass();
      } catch (IllegalAccessException ex) {
        throw new DeadExcelException("写入字段失败：" + fieldName, ex);
      }
    }
    throw new DeadExcelException("字段不存在：" + fieldName);
  }
}
