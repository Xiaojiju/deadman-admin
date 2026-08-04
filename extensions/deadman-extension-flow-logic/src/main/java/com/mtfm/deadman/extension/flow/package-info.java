/**
 * 可复用串行流程引擎（FilterChain 风格）。
 * <p>
 * 业务模块自定义 {@link com.mtfm.deadman.extension.flow.FlowContext} 子类与节点，
 * 通过 {@link com.mtfm.deadman.extension.flow.AbstractFlowChain} 编排后由
 * {@link com.mtfm.deadman.extension.flow.FlowChainRegistry} 按流程类型查找执行。
 */
package com.mtfm.deadman.extension.flow;
