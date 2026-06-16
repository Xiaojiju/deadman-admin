package com.mtfm.deadman.plugin.datascope.support;

import com.mtfm.deadman.plugin.datascope.model.DataColumnSpec;
import com.mtfm.deadman.plugin.datascope.model.DataScopeUserContext;
import com.mtfm.deadman.plugin.datascope.model.DataScopeType;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 根据数据范围与列映射构建 JSqlParser 条件表达式。
 */
@Component
public class DataScopeExpressionBuilder {

    /**
     * 构建追加到 WHERE 的数据权限条件。
     *
     * @param table   SQL 表节点
     * @param spec    列映射
     * @param context 用户数据范围上下文
     * @return 条件表达式；无需过滤时返回 null
     */
    public Expression build(Table table, DataColumnSpec spec, DataScopeUserContext context) {
        if (context.bypass() || context.scopeType() == DataScopeType.ALL) {
            return null;
        }
        return switch (context.scopeType()) {
            case SELF -> buildSelfExpression(table, spec, context);
            case DEPT, DEPT_AND_CHILD, CUSTOM -> buildDeptExpression(table, spec, context);
            case ALL -> null;
        };
    }

    private Expression buildSelfExpression(Table table, DataColumnSpec spec, DataScopeUserContext context) {
        if (!StringUtils.hasText(spec.userColumn())) {
            return denyAll();
        }
        return new EqualsTo(qualifiedColumn(table, spec.userColumn()), new LongValue(context.userId()));
    }

    private Expression buildDeptExpression(Table table, DataColumnSpec spec, DataScopeUserContext context) {
        if (StringUtils.hasText(spec.deptJoinTable())) {
            return buildDeptJoinExpression(table, spec, context);
        }
        if (!StringUtils.hasText(spec.deptColumn())) {
            return buildSelfExpression(table, spec, context);
        }
        Set<Long> deptIds = context.visibleDeptIds();
        if (deptIds.isEmpty()) {
            return buildSelfExpression(table, spec, context);
        }
        Column deptColumn = qualifiedColumn(table, spec.deptColumn());
        return inExpression(deptColumn, deptIds);
    }

    private Expression buildDeptJoinExpression(Table table, DataColumnSpec spec, DataScopeUserContext context) {
        if (!StringUtils.hasText(spec.userColumn())) {
            return buildSelfExpression(table, spec, context);
        }
        Set<Long> deptIds = context.visibleDeptIds();
        if (deptIds.isEmpty()) {
            return buildSelfExpression(table, spec, context);
        }

        Table joinTable = new Table(spec.deptJoinTable());
        PlainSelect subSelect = new PlainSelect();
        subSelect.addSelectItems(new Column(spec.deptJoinUserColumn()));
        subSelect.setFromItem(joinTable);

        EqualsTo userMatch = new EqualsTo(
                new Column(joinTable, spec.deptJoinUserColumn()),
                qualifiedColumn(table, spec.userColumn()));

        Expression deptFilter = inExpression(new Column(joinTable, spec.deptJoinDeptColumn()), deptIds);
        List<Expression> conditions = new ArrayList<>();
        conditions.add(userMatch);
        conditions.add(deptFilter);
        if (spec.deptJoinPrimaryOnly()) {
            conditions.add(new EqualsTo(new Column(joinTable, "is_primary"), new LongValue(1)));
        }
        subSelect.setWhere(combineWithAnd(conditions));

        ParenthesedSelect parenthesedSelect = new ParenthesedSelect(subSelect);

        InExpression inExpression = new InExpression();
        inExpression.setLeftExpression(qualifiedColumn(table, spec.userColumn()));
        inExpression.setRightExpression(parenthesedSelect);
        return inExpression;
    }

    private Expression inExpression(Column column, Set<Long> deptIds) {
        if (deptIds.size() == 1) {
            return new EqualsTo(column, new LongValue(deptIds.iterator().next()));
        }
        List<Expression> values = new ArrayList<>(deptIds.size());
        deptIds.forEach(id -> values.add(new LongValue(id)));
        InExpression inExpression = new InExpression();
        inExpression.setLeftExpression(column);
        inExpression.setRightExpression(new ParenthesedExpressionList<>(values));
        return inExpression;
    }

    private Expression combineWithAnd(List<Expression> expressions) {
        Expression result = expressions.get(0);
        for (int i = 1; i < expressions.size(); i++) {
            result = new AndExpression(result, expressions.get(i));
        }
        return result;
    }

    private Expression denyAll() {
        return new EqualsTo(new LongValue(1), new LongValue(0));
    }

    private Column qualifiedColumn(Table table, String columnName) {
        StringBuilder builder = new StringBuilder();
        if (table.getAlias() != null && StringUtils.hasText(table.getAlias().getName())) {
            builder.append(table.getAlias().getName()).append('.');
        }
        builder.append(columnName);
        return new Column(builder.toString());
    }
}
