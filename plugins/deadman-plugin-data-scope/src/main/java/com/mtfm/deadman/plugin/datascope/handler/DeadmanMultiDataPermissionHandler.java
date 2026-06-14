package com.mtfm.deadman.plugin.datascope.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.mtfm.deadman.plugin.datascope.context.DataScopeContextHolder;
import com.mtfm.deadman.plugin.datascope.context.DataScopeRequestContextHolder;
import com.mtfm.deadman.plugin.datascope.model.DataColumnSpec;
import com.mtfm.deadman.plugin.datascope.model.DataScopeUserContext;
import com.mtfm.deadman.plugin.datascope.support.DataColumnMetadataResolver;
import com.mtfm.deadman.plugin.datascope.support.DataScopeExpressionBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import org.springframework.stereotype.Component;

/**
 * MyBatis-Plus 多表数据权限处理器：在 {@code @DataScope} 调用链内读取 {@code @DataColumn} 并追加
 * WHERE 条件。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeadmanMultiDataPermissionHandler implements MultiDataPermissionHandler {

    private final DataColumnMetadataResolver columnMetadataResolver;
    private final DataScopeExpressionBuilder expressionBuilder;

    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        if (!DataScopeContextHolder.isEnabled() || DataScopeContextHolder.isIgnored()) {
            return null;
        }
        DataScopeUserContext context = DataScopeRequestContextHolder.get();
        if (context == null || context.bypass()) {
            return null;
        }
        DataColumnSpec spec = columnMetadataResolver.resolve(mappedStatementId, table.getName());
        if (spec == null || !spec.hasAnyColumn()) {
            return null;
        }
        Expression expression = expressionBuilder.build(table, spec, context);
        if (log.isDebugEnabled() && expression != null) {
            log.debug("数据权限追加 mappedStatementId={} table={} expr={}", mappedStatementId, table.getName(), expression);
        }
        return expression;
    }
}
