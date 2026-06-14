package com.mtfm.deadman.plugin.datascope.support;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtfm.deadman.plugin.datascope.annotation.DataColumn;
import com.mtfm.deadman.plugin.datascope.config.DataScopePluginProperties;
import com.mtfm.deadman.plugin.datascope.model.DataColumnSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 解析 Mapper {@link DataColumn} 元数据并缓存；YAML 配置仅作可选覆盖/兜底。
 * <p>
 * 在 {@link ContextRefreshedEvent} 后从 MyBatis {@link SqlSessionFactory}
 * 加载索引，避免构造期与 SqlSessionFactory 环依赖。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataColumnMetadataResolver {

    private final ObjectProvider<SqlSessionFactory> sqlSessionFactoryProvider;
    private final DataScopePluginProperties properties;
    private final AtomicBoolean indexed = new AtomicBoolean(false);

    /** key：物理表名（小写） */
    private final Map<String, DataColumnSpec> tableIndex = new ConcurrentHashMap<>();

    /** key：Mapper 全限定类名 */
    private final Map<String, DataColumnSpec> mapperIndex = new ConcurrentHashMap<>();

    /** key：MappedStatement Id 前缀（Mapper 类名 + 方法名） */
    private final Map<String, DataColumnSpec> statementIndex = new ConcurrentHashMap<>();

    /**
     * Spring 上下文刷新完成后加载 {@link DataColumn} 索引（仅根上下文执行一次）。
     *
     * @param event 上下文刷新事件
     */
    @EventListener(ContextRefreshedEvent.class)
    public void loadColumnMetadata(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() != null) {
            return;
        }
        ensureIndexed();
    }

    /**
     * 根据 MappedStatement 与物理表名解析列映射。
     *
     * @param mappedStatementId MyBatis MappedStatement Id
     * @param physicalTableName SQL 中的物理表名
     * @return 列映射，未声明时返回 null
     */
    public DataColumnSpec resolve(String mappedStatementId, String physicalTableName) {
        ensureIndexed();
        DataColumnSpec spec = resolveFromStatement(mappedStatementId);
        if (spec == null && StringUtils.hasText(physicalTableName)) {
            spec = tableIndex.get(physicalTableName.trim().toLowerCase());
        }
        if (spec == null && StringUtils.hasText(physicalTableName)) {
            spec = resolveFromProperties(physicalTableName.trim().toLowerCase());
        }
        return spec;
    }

    /**
     * 从 MyBatis 已注册的 Mapper 接口构建索引（幂等）。
     */
    private void ensureIndexed() {
        if (indexed.get()) {
            return;
        }
        synchronized (this) {
            if (indexed.get()) {
                return;
            }
            SqlSessionFactory sqlSessionFactory = sqlSessionFactoryProvider.getIfAvailable();
            if (sqlSessionFactory == null) {
                return;
            }
            int mapperCount = 0;
            for (Class<?> mapperClass : sqlSessionFactory.getConfiguration().getMapperRegistry().getMappers()) {
                indexMapperClass(mapperClass);
                mapperCount++;
            }
            indexed.set(true);
            log.debug("数据权限列映射索引完成：扫描 Mapper {} 个，表 {} 个", mapperCount, tableIndex.size());
        }
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
