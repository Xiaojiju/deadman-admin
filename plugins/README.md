# Deadman 插件目录

本目录存放**可插拔**的支撑模块，与 `deadman-system`、`deadman-security` 等核心分层解耦。插件仅依赖 `deadman-common`、`deadman-core`，通过 SPI 与业务集成。

## 插拔方式

1. 在 `deadman-app/pom.xml` 中增加对应插件依赖（例如 `deadman-plugin-websocket`）。
2. 在 `application.yaml` 中开启/关闭：`deadman.plugin.<name>.enabled`。
3. 移除依赖或设置 `enabled: false` 即可停用，不影响核心模块编译。

## 已有插件

### deadman-plugin-websocket

基于 Spring WebSocket 的消息通道插件，支持：

| 能力 | 说明 |
|------|------|
| 消息通道 | 业务侧通过 `MessageChannelFactory` 声明 Spring Bean，按用户体系隔离 |
| 统一负载体 | `WsMessage`，通道可继承扩展 |
| 调度器 | `MessageDispatcher`：持久化 → 投递 → 重试 |
| 持久化 | 表 `plugin_ws_message` |
| 拦截器 | `MessageDeliveryInterceptor`，投递成功或最终失败后回调 |

#### 连接地址

```
ws://{host}:{port}/ws/{channelCode}?token={jwt}
```

管理端示例：`/ws/admin?token=...`（需实现 `WebSocketAuthenticator`，见 `AdminJwtWebSocketAuthenticator`）。

#### 注册通道 Bean

```java
@Configuration
public class WebSocketChannelConfiguration {

    @Bean
    MessageChannel adminMessageChannel(MessageChannelFactory factory) {
        return factory.create("admin", "管理端系统用户体系");
    }
}
```

#### 发送消息

```java
@Autowired
@Qualifier("adminMessageChannel")
private MessageChannel adminMessageChannel;

adminMessageChannel.dispatch(
        "ORDER_PAID",
        String.valueOf(userId),
        Map.of("orderId", orderId));
```

#### 扩展通道消息体

```java
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class AdminWsMessage extends WsMessage {
    private String scene;
}
```

#### 自定义投递拦截

```java
@Component
public class AuditDeliveryInterceptor implements MessageDeliveryInterceptor {
    @Override
    public void afterDelivery(MessageDeliveryContext context) {
        // 审计、告警等
    }
}
```

#### 技术选型说明

当前采用 **原生 Spring WebSocket + JSON**，适合服务端推送与多通道隔离。若后续需要 STOMP 订阅、主题广播或跨节点 fan-out，可在此基础上引入 STOMP 或 Redis Pub/Sub，插件 SPI（`MessageTransport`、`WebSocketAuthenticator`）可替换实现。

## 新增插件规范

1. 在 `plugins/` 下新建子模块，父 POM 为 `deadman-plugins`。
2. 在根 `pom.xml` 的 `dependencyManagement` 中登记 artifact。
3. 使用 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 提供自动配置。
4. 不反向依赖 `deadman-system` / `deadman-security`；业务适配放在 `deadman-app` 或独立 bridge 模块。
