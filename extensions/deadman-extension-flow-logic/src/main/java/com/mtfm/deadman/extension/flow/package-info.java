/**
 * 可复用流程引擎：普通节点、互斥/并行网关、结束节点、错误兜底与节点成功事件。
 * <p>
 * 核心协作：
 * <ul>
 *   <li>{@link com.mtfm.deadman.extension.flow.FlowContext}：共享状态（abort/attrs 并发安全）</li>
 *   <li>{@link com.mtfm.deadman.extension.flow.FlowElement#run}：元素自执行</li>
 *   <li>{@link com.mtfm.deadman.extension.flow.error.FlowErrorHandler}：失败兜底</li>
 *   <li>{@link com.mtfm.deadman.extension.flow.event.FlowNodeEventListener}：节点成功通知</li>
 *   <li>{@link com.mtfm.deadman.extension.flow.AbstractFlowChain}：编排 DSL</li>
 * </ul>
 */
package com.mtfm.deadman.extension.flow;
