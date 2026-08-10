# deadman-extension-flow-logic

可复用流程引擎：节点多态 + 引擎驱动。支持普通节点、互斥/并行网关、结束节点、错误兜底与节点成功事件。

## 能力一览

| 类型 | 说明 |
|------|------|
| `FlowContext` | 上下文（abort/attrs 并发安全；并行须 `forkForParallelBranch`） |
| `LogicNode` / `EndNode` | 普通节点 / 结束节点（`run` 自执行；节点 Bean 须无请求间可变状态） |
| `ExclusiveGateway` | 互斥网关（条件 + 强制默认） |
| `ParallelGateway` | 并行网关（分支隔离 fork；共享虚拟线程执行器） |
| `FlowErrorHandler` | 失败兜底（**必须抛异常**通知调用方） |
| `FlowNodeEventListener` / `FlowEventPublisher` | 节点成功事件（after 拦截器之后触发） |
| `AbstractFlowChain` | 编排 DSL（EndNode 须在链尾；elementId 唯一） |

## 执行顺序（单元素）

`before` → `run` → `after` →（若为业务节点）成功事件

拦截器异常同样进入 `FlowErrorHandler`。

## 错误处理（强制抛出）

```java
FlowErrorHandler<LoginContext> handler = event -> {
    if (event.error() instanceof IllegalArgumentException) {
        throw new BusinessException(ResultCode.BAD_REQUEST, "用户名格式错误", event.error());
    }
    PropagatingFlowErrorHandler.<LoginContext>instance().handle(event);
};
```

`handle` 若正常返回，引擎会抛出「必须抛出异常」的编程错误，禁止静默成功。

嵌套子链失败时，错误事件优先携带**最内层**节点 ID（`FlowElementException`）。

## 节点成功事件

1. 链级：`FlowNodeEventListener`，在 after 之后自动回调  
2. 节点级：注入 `FlowEventPublisher`，在 `execute` 内 `publishSuccess(context)`

## 业务侧必须遵守的约束

引擎已用构造期/运行期校验兜住一部分误用；下列约定**无法完全由框架代劳**，业务接入时必须遵守。

### 1. 并行与写库

- 并行分支即使做了 `fork`，`Future.cancel` 仍**无法可靠中止**已发出的 JDBC/MyBatis 调用。
- 因此并行**只允许**用于：无共享副作用、只读分流，或分支写操作**可幂等 / 可补偿**的场景。
- **禁止**对「下单落库、扣库存、支付落单」等不可轻易回滚的写路径直接开 `ParallelGateway`。
- 订单类主链路请保持串行；通知、埋点等旁路才考虑并行。

### 2. Context 并行隔离（使用并行时强制）

- 业务 `FlowContext` 子类若要用并行，**必须**覆盖 `forkForParallelBranch()`。
- 必须返回**新实例**，不得 `return this`；并用 `copyBaseStateTo(branch)` 同步 abort/attrs。
- 分支内只读输入可共享不可变引用；**不得**把可变业务字段写回父上下文实例。
- 未实现 fork 时调用并行网关会直接失败（框架强制）。

```java
@Override
public FlowContext forkForParallelBranch() {
    MyContext branch = new MyContext(/* 只复制只读输入 */);
    copyBaseStateTo(branch);
    return branch;
}
```

### 3. 节点 Bean 单例安全

- `LogicNode` / `EndNode` 通常是 Spring **单例** Bean，会并发服务多个请求。
- 节点内**禁止**使用请求级可变实例字段缓存业务状态；状态一律放进当次 `FlowContext`。
- 允许注入无状态或线程安全的依赖（Mapper、Service 等）。

### 4. 错误处理与调用方判定

- `FlowErrorHandler.handle` **必须抛异常**（推荐 `BusinessException`），禁止正常返回「吞掉」错误。
- 执行器在「未抛错」之外，仍应检查业务结果是否产出（如结算 VO 非空），以及 `context.isAborted()`（节点主动 abort 的场景，例如重复支付回调忽略）。

### 5. 编排结构

- `EndNode` 若存在，必须位于链尾（框架校验）。
- 同一条链顶层 `elementId` / `nodeId` 必须唯一（框架校验）。
- 链构造期会 `BeanFactory.getBean` 解析节点：避免链与节点循环依赖；复杂场景优先让节点不依赖具体 Chain 类型。

## 编排示例

```java
public CheckoutChain(BeanFactory bf) {
    super(FlowType.CHECKOUT, bf, List.of(), handler, List.of(auditListener));
}

@Override
protected void configure() {
    addLogic(ValidateFormNode.class);
    addExclusive("logistics",
        ExclusiveBranch.of(ctx -> ctx.isSeafood(), coldNode),
        ExclusiveBranch.defaultOf(normalNode));
    addEnd(BuildResultEndNode.class);
}
```

## 依赖

- `deadman-common`
- Spring Beans / Context
