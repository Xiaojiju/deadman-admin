# deadman-extension-flow-logic

可复用的串行流程引擎（FilterChain 风格）：节点按插入顺序执行，节点内调用 `chain.proceed(context)` 推进下一环。

## 能力

| 类型 | 说明 |
|------|------|
| `FlowContext` | 流程上下文基类（中断标记、扩展 attrs） |
| `FlowNode` | 串行节点 |
| `FlowChain` | 流程链（`proceed`） |
| `AbstractFlowChain` | 编排（`addNode` / `addNodes`）+ VirtualFilterChain 推进 |
| `FlowInterceptor` | 节点前后横切 |
| `FlowBranch` | 挂在主节点之后的支流 |
| `FlowChainRegistry` | 按流程类型键注册 / 查找链 |

## 使用方式

业务模块依赖本 extension，自行定义：

1. 上下文：`extends FlowContext`
2. 流程类型枚举（或其它键类型 `K`）
3. 节点：`implements FlowNode<YourContext>`
4. 链：`extends AbstractFlowChain<K, YourContext>`，在 `configure()` 中 `addNodes(...)`
5. 执行器：注入各链实例，用 `FlowChainRegistry.of(...)` 按类型查找

> 若同一应用内有多套流程且需按类型注入 `List`，可自行增加领域 Marker 接口；无此需求时直接使用通用类型即可。

```java
@Component
public class CheckoutXxxFlowChain extends AbstractFlowChain<XxxFlowType, XxxFlowContext> {

    public CheckoutXxxFlowChain(BeanFactory beanFactory) {
        super(XxxFlowType.CHECKOUT, beanFactory, List.of(), List.of());
    }

    @Override
    protected void configure() {
        addNodes(ResolveAddressNode.class, CreateOrderNode.class);
    }
}
```

## 依赖

- `deadman-common`（`BusinessException` / `ResultCode`）
- Spring Beans / Context（`BeanFactory` 按类型解析节点）

本模块为纯库，**无**强制 AutoConfiguration；引入依赖即可使用。
