package com.mtfm.deadman.extension.flow.gateway;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.extension.flow.FlowChain;
import com.mtfm.deadman.extension.flow.FlowContext;
import com.mtfm.deadman.extension.flow.FlowElement;

import lombok.extern.slf4j.Slf4j;

/**
 * 互斥网关（XOR）：按条件选择唯一分支；均不满足时走强制默认分支。
 *
 * @param <C> 上下文类型
 */
@Slf4j
public final class ExclusiveGateway<C extends FlowContext> implements FlowElement<C> {

    /** 网关元素 ID */
    private final String elementId;

    /** 条件分支（不含默认） */
    private final List<ExclusiveBranch<C>> conditionalBranches;

    /** 默认分支目标 */
    private final FlowChain<?, C> defaultTarget;

    /**
     * @param elementId 网关 ID
     * @param branches 分支列表（须恰好一个默认分支）
     */
    public ExclusiveGateway(String elementId, List<ExclusiveBranch<C>> branches) {
        this.elementId = Objects.requireNonNull(elementId, "elementId");
        Objects.requireNonNull(branches, "branches");
        List<ExclusiveBranch<C>> conditionals = new ArrayList<>();
        FlowChain<?, C> defaultChain = null;
        for (ExclusiveBranch<C> branch : branches) {
            Objects.requireNonNull(branch, "branch");
            if (branch.defaultBranch()) {
                if (defaultChain != null) {
                    throw new BusinessException(ResultCode.BAD_REQUEST, "互斥网关只能有一个默认分支: " + elementId);
                }
                defaultChain = branch.target();
            } else {
                conditionals.add(branch);
            }
        }
        if (defaultChain == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "互斥网关必须配置默认分支: " + elementId);
        }
        this.conditionalBranches = List.copyOf(conditionals);
        this.defaultTarget = defaultChain;
    }

    @Override
    public String elementId() {
        return elementId;
    }

    @Override
    public boolean run(C context) {
        route(context);
        return false;
    }

    /**
     * 评估条件并执行命中（或默认）子链。
     *
     * @param context 流程上下文
     */
    public void route(C context) {
        for (ExclusiveBranch<C> branch : conditionalBranches) {
            if (branch.condition().test(context)) {
                log.debug("互斥网关命中条件分支: gatewayId={}, target={}", elementId, branch.target().flowType());
                branch.target().execute(context);
                return;
            }
        }
        log.debug("互斥网关走默认分支: gatewayId={}, target={}", elementId, defaultTarget.flowType());
        defaultTarget.execute(context);
    }
}
