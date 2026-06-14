package com.mtfm.deadman.plugin.datascope.support;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtfm.deadman.plugin.datascope.annotation.DataColumn;
import com.mtfm.deadman.plugin.datascope.config.DataScopePluginProperties;
import com.mtfm.deadman.plugin.datascope.model.DataColumnSpec;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 解析 Mapper {@link DataColumn} 元数据并缓存；YAML 配置仅作可选覆盖/兜底。
 */
@Component
@RequiredArgsConstructor
public class DataColumnMetadataResolver {

    private final ApplicationContext applicationContext;
    private final DataScopePluginProperties properties;

    /** key：物理表名（小写） */
    private final Map<String, DataColumnSpec> tableIndex = new ConcurrentHashMap<>();

    /** key：Mapper 全限定类名 */
    private final Map<String, DataColumnSpec> mapperIndex = new ConcurrentHashMap<>();

    /** key：MappedStatement Id 前缀（Mapper 类名 + 方法名） */
    private final Map<String, DataColumnSpec> statementIndex = new ConcurrentHashMap<>();

    /**
     * 启动时扫描所有 {@link Mapper} Bean，索引 {@link DataColumn} 声明。
     */
    @PostConstruct
    void indexMapperAnnotations() {
        Map<String, Object> mappers = applicationContext.getBeansWithAnnotation(Mapper.class);
        for (Object mapperBean : mappers.values()) {
            Class<?> mapperClass = ClassUtils.getUserClass(mapperBean);
            indexMapperClass(mapperClass);
        }
    }

    /**
     * 根据 MappedStatement 与物理表名解析列映射。
     *
     * @param mappedStatementId MyBatis MappedStatement Id
     * @param physicalTableName SQL 中的物理表名
     * @return 列映射，未声明时返回 null
     */
    public DataColumnSpec resolve(String mappedStatementId, String physicalTableName) {
        DataColumnSpec spec = resolveFromStatement(mappedStatementId);
        if (spec == null && StringUtils.hasText(physicalTableName)) {
            spec = tableIndex.get(physicalTableName.trim().toLowerCase());
        }
        if (spec == null && StringUtils.hasText(physicalTableName)) {
            spec = resolveFromProperties(physicalTableName.trim().toLowerCase());
        }
        return spec;
    }

    private DataColumnSpec resolveFromStatement(String mappedStatementId) {
        if (!StringUtils.hasText(mappedStatementId)) {
            return null;
        }
        DataColumnSpec exact = statementIndex.get(mappedStatementId);
        if (exact != null) {
            return exact;
        }
        int lastDot = mappedStatementId.lastIndexOf('.');
        if (lastDot <= 0) {
            return null;
        }
        String mapperClassName = mappedStatementId.substring(0, lastDot);
        return mapperIndex.get(mapperClassName);
    }

    private void indexMapperClass(Class<?> mapperClass) {
        DataColumn classColumn = AnnotationUtils.findAnnotation(mapperClass, DataColumn.class);
        if (classColumn != null) {
            registerSpec(mapperClass, null, classColumn);
        }
        for (Method method : mapperClass.getMethods()) {
            DataColumn methodColumn = AnnotationUtils.findAnnotation(method, DataColumn.class);
            if (methodColumn != null) {
                registerSpec(mapperClass, method.getName(), methodColumn);
            }
        }
    }

    private void registerSpec(Class<?> mapperClass, String methodName, DataColumn dataColumn) {
        String tableName = resolveTableName(mapperClass, dataColumn);
        DataColumnSpec spec = toSpec(tableName, dataColumn);
        if (!spec.hasAnyColumn()) {
            return;
        }
        mapperIndex.putIfAbsent(mapperClass.getName(), spec);
        if (StringUtils.hasText(tableName)) {
            tableIndex.putIfAbsent(tableName, spec);
        }
        if (StringUtils.hasText(methodName)) {
            statementIndex.putIfAbsent(mapperClass.getName() + "." + methodName, spec);
        }
    }

    private DataColumnSpec resolveFromProperties(String tableName) {
        DataScopePluginProperties.TableColumnMapping mapping = properties.getColumns().get(tableName);
        if (mapping == null) {
            return null;
        }
        if (!StringUtils.hasText(mapping.getDeptColumn()) && !StringUtils.hasText(mapping.getUserColumn())) {
            return null;
        }
        return new DataColumnSpec(tableName, mapping.getDeptColumn(), mapping.getUserColumn());
    }

    private static DataColumnSpec toSpec(String tableName, DataColumn dataColumn) {
        return new DataColumnSpec(
                tableName,
                blankToNull(dataColumn.dept()),
                blankToNull(dataColumn.user()));
    }

    private static String resolveTableName(Class<?> mapperClass, DataColumn dataColumn) {
        if (StringUtils.hasText(dataColumn.table())) {
            return dataColumn.table().trim().toLowerCase();
        }
        ResolvableType mapperType = ResolvableType.forClass(mapperClass).as(BaseMapper.class);
        Class<?> entityClass = mapperType.getGeneric(0).resolve();
        if (entityClass == null) {
            return null;
        }
        TableName tableName = AnnotationUtils.findAnnotation(entityClass, TableName.class);
        if (tableName == null || !StringUtils.hasText(tableName.value())) {
            return null;
        }
        return tableName.value().trim().toLowerCase();
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
