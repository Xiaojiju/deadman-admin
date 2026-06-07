package com.mtfm.deadman.plugin.excel.metadata;

import com.mtfm.deadman.plugin.excel.annotation.DeadExcelColumn;
import com.mtfm.deadman.plugin.excel.column.DeadExcelColumnDefinition;
import com.mtfm.deadman.plugin.excel.exception.DeadExcelException;
import com.mtfm.deadman.plugin.excel.support.DeadExcelCellConverter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 解析 POJO / Record 的 Excel 列元数据，并负责行对象与单元格数据的互转。
 */
public final class DeadExcelMetadata {

  private static final Map<Class<?>, DeadExcelMetadata> CACHE = new ConcurrentHashMap<>();

  private final Class<?> type;
  private final List<DeadExcelColumnBinding> bindings;

  private DeadExcelMetadata(Class<?> type, List<DeadExcelColumnBinding> bindings) {
    this.type = type;
    this.bindings = List.copyOf(bindings);
  }

  /**
   * 获取并缓存类型的 Excel 元数据。
   *
   * @param type 目标类型
   * @return 元数据
   */
  public static DeadExcelMetadata of(Class<?> type) {
    return CACHE.computeIfAbsent(type, DeadExcelMetadata::resolve);
  }

  /**
   * 转为列定义列表。
   *
   * @return 列定义
   */
  public List<DeadExcelColumnDefinition> toColumnDefinitions() {
    return bindings.stream()
        .map(
            binding ->
                DeadExcelColumnDefinition.builder()
                    .fieldName(binding.getFieldName())
                    .header(binding.getHeader())
                    .index(binding.getIndex())
                    .width(binding.getWidth())
                    .dateFormat(binding.getDateFormat())
                    .build())
        .toList();
  }

  /**
   * 构建 EasyExcel 动态表头。
   *
   * @return 表头行
   */
  public List<List<String>> buildHead() {
    return bindings.stream().map(binding -> List.of(binding.getHeader())).toList();
  }

  /**
   * 从行对象提取一行单元格数据。
   *
   * @param row 行对象
   * @return 单元格值列表
   */
  public List<Object> extractRow(Object row) {
    List<Object> values = new ArrayList<>(bindings.size());
    for (DeadExcelColumnBinding binding : bindings) {
      Object raw = readValue(row, binding.getFieldName());
      values.add(DeadExcelCellConverter.formatForWrite(raw, binding.getDateFormat()));
    }
    return values;
  }

  /**
   * 根据单元格数据创建行对象实例。
   *
   * @param cells       列序号 -> 单元格文本
   * @param headerIndex 表头名称 -> 列序号
   * @param matchByHeader 是否按表头匹配
   * @return 行对象
   */
  public Object createInstance(
      Map<Integer, String> cells, Map<String, Integer> headerIndex, boolean matchByHeader) {
    if (type.isRecord()) {
      return createRecord(cells, headerIndex, matchByHeader);
    }
    return createPojo(cells, headerIndex, matchByHeader);
  }

  private static DeadExcelMetadata resolve(Class<?> type) {
    List<DeadExcelColumnBinding> bindings = new ArrayList<>();
    boolean annotatedOnly = hasAnnotatedColumns(type);
    if (type.isRecord()) {
      resolveRecord(type, bindings, annotatedOnly);
    } else {
      resolvePojo(type, bindings, annotatedOnly);
    }
    if (bindings.isEmpty()) {
      throw new DeadExcelException("类型未定义可导入导出的列：" + type.getName());
    }
    bindings.sort(Comparator.comparingInt(DeadExcelColumnBinding::getIndex));
    return new DeadExcelMetadata(type, bindings);
  }

  private static boolean hasAnnotatedColumns(Class<?> type) {
    if (type.isRecord()) {
      for (RecordComponent component : type.getRecordComponents()) {
        if (component.getAnnotation(DeadExcelColumn.class) != null) {
          return true;
        }
      }
      return false;
    }
    for (Field field : getAllFields(type)) {
      if (field.getAnnotation(DeadExcelColumn.class) != null) {
        return true;
      }
    }
    return false;
  }

  private static void resolveRecord(
      Class<?> type, List<DeadExcelColumnBinding> bindings, boolean annotatedOnly) {
    RecordComponent[] components = type.getRecordComponents();
    for (int i = 0; i < components.length; i++) {
      RecordComponent component = components[i];
      DeadExcelColumn annotation = component.getAnnotation(DeadExcelColumn.class);
      if (annotatedOnly && annotation == null) {
        continue;
      }
      if (annotation != null && annotation.ignore()) {
        continue;
      }
      bindings.add(toBinding(component.getName(), component.getType(), annotation, i));
    }
  }

  private static void resolvePojo(
      Class<?> type, List<DeadExcelColumnBinding> bindings, boolean annotatedOnly) {
    List<Field> fields = getAllFields(type);
    int order = 0;
    for (Field field : fields) {
      if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
        continue;
      }
      DeadExcelColumn annotation = field.getAnnotation(DeadExcelColumn.class);
      if (annotatedOnly && annotation == null) {
        continue;
      }
      if (annotation != null && annotation.ignore()) {
        continue;
      }
      bindings.add(toBinding(field.getName(), field.getType(), annotation, order++));
    }
  }

  private static DeadExcelColumnBinding toBinding(
      String fieldName, Class<?> fieldType, DeadExcelColumn annotation, int defaultIndex) {
    if (annotation == null) {
      return DeadExcelColumnBinding.builder()
          .fieldName(fieldName)
          .header(fieldName)
          .index(defaultIndex)
          .width(20)
          .fieldType(fieldType)
          .build();
    }
    String header = annotation.value().isBlank() ? fieldName : annotation.value();
    return DeadExcelColumnBinding.builder()
        .fieldName(fieldName)
        .header(header)
        .index(annotation.index() >= 0 ? annotation.index() : defaultIndex)
        .width(annotation.width())
        .dateFormat(annotation.dateFormat().isBlank() ? null : annotation.dateFormat())
        .fieldType(fieldType)
        .build();
  }

  private static List<Field> getAllFields(Class<?> type) {
    List<Field> fields = new ArrayList<>();
    Class<?> current = type;
    while (current != null && current != Object.class) {
      fields.addAll(List.of(current.getDeclaredFields()));
      current = current.getSuperclass();
    }
    return fields;
  }

  private Object createRecord(
      Map<Integer, String> cells, Map<String, Integer> headerIndex, boolean matchByHeader) {
    RecordComponent[] components = type.getRecordComponents();
    Object[] args = new Object[components.length];
    Class<?>[] paramTypes = new Class<?>[components.length];
    for (int i = 0; i < components.length; i++) {
      RecordComponent component = components[i];
      paramTypes[i] = component.getType();
      DeadExcelColumnBinding binding = findBinding(component.getName());
      if (binding == null) {
        args[i] = null;
        continue;
      }
      String cellText = resolveCellText(binding, cells, headerIndex, matchByHeader);
      args[i] = DeadExcelCellConverter.convert(cellText, binding.getFieldType(), binding.getDateFormat());
    }
    try {
      Constructor<?> constructor = type.getDeclaredConstructor(paramTypes);
      constructor.setAccessible(true);
      return constructor.newInstance(args);
    } catch (ReflectiveOperationException ex) {
      throw new DeadExcelException("实例化 Record 失败：" + type.getName(), ex);
    }
  }

  private Object createPojo(
      Map<Integer, String> cells, Map<String, Integer> headerIndex, boolean matchByHeader) {
    try {
      Constructor<?> constructor = type.getDeclaredConstructor();
      constructor.setAccessible(true);
      Object instance = constructor.newInstance();
      for (DeadExcelColumnBinding binding : bindings) {
        String cellText = resolveCellText(binding, cells, headerIndex, matchByHeader);
        Object value =
            DeadExcelCellConverter.convert(cellText, binding.getFieldType(), binding.getDateFormat());
        writeValue(instance, binding.getFieldName(), value);
      }
      return instance;
    } catch (ReflectiveOperationException ex) {
      throw new DeadExcelException("实例化 POJO 失败：" + type.getName(), ex);
    }
  }

  private DeadExcelColumnBinding findBinding(String fieldName) {
    return bindings.stream()
        .filter(binding -> binding.getFieldName().equals(fieldName))
        .findFirst()
        .orElse(null);
  }

  private static String resolveCellText(
      DeadExcelColumnBinding binding,
      Map<Integer, String> cells,
      Map<String, Integer> headerIndex,
      boolean matchByHeader) {
    Integer columnIndex = binding.getIndex();
    if (matchByHeader && headerIndex.containsKey(binding.getHeader())) {
      columnIndex = headerIndex.get(binding.getHeader());
    }
    if (columnIndex == null) {
      return null;
    }
    return cells.get(columnIndex);
  }

  private static Object readValue(Object row, String fieldName) {
    if (row instanceof Map<?, ?> map) {
      return map.get(fieldName);
    }
    Class<?> rowType = row.getClass();
    if (rowType.isRecord()) {
      for (RecordComponent component : rowType.getRecordComponents()) {
        if (component.getName().equals(fieldName)) {
          try {
            return component.getAccessor().invoke(row);
          } catch (ReflectiveOperationException ex) {
            throw new DeadExcelException("读取 Record 字段失败：" + fieldName, ex);
          }
        }
      }
      return null;
    }
    return readPojoField(row, rowType, fieldName);
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

  private static void writeValue(Object row, String fieldName, Object value) {
    Class<?> current = row.getClass();
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
