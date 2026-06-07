# Deadman 插件目录

本目录存放**可插拔**的支撑模块，与 `deadman-system`、`deadman-security` 等核心分层解耦。插件通过 SPI 或自动配置接入，在 `deadman-app` 中按需引入依赖即可启用。

## 插件一览

| 模块 | 配置前缀 | 职责 | OpenAPI |
|------|----------|------|---------|
| [deadman-plugin-websocket](#deadman-plugin-websocket) | `deadman.plugin.websocket` | WebSocket 多通道消息、持久化与重试 | — |
| [deadman-plugin-wechat](#deadman-plugin-wechat) | `deadman.plugin.wechat-miniprogram` | 微信小程序登录（多端 login-bindings）、OAuth/手机号 SPI | [WechatMiniprogramController.yaml](../doc/deadman-plugin-wechat/WechatMiniprogramController.yaml) |
| [deadman-plugin-excel](#deadman-plugin-excel) | `deadman.plugin.excel` | EasyExcel 导入导出工具包 | — |
| [deadman-plugin-file](#deadman-plugin-file) | `deadman.plugin.file` | 文件上传下载、`FileStorageProvider` SPI | [FileController.yaml](../doc/deadman-plugin-file/FileController.yaml) |
| [deadman-plugin-storage-local](#deadman-plugin-storage-local) | `deadman.plugin.storage-local` | 本地磁盘存储 Provider（依赖 file 插件） | 见 file 文档 `/files/**` |

## 插拔方式

1. 在根 `pom.xml` 的 `<modules>` 与 `<dependencyManagement>` 中登记新插件（已有插件可跳过）。
2. 在 `deadman-app/pom.xml` 中增加依赖，例如：

   ```xml
   <dependency>
       <groupId>com.mtfm</groupId>
       <artifactId>deadman-plugin-excel</artifactId>
   </dependency>
   ```

3. 在 `application.yaml` 中配置并开关：`deadman.plugin.<kebab-name>.enabled`（环境变量 `DEADMAN_PLUGIN_<NAME>_ENABLED`）。
4. 移除依赖或设置 `enabled: false` 即可停用，不影响核心模块编译。

各插件通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册 `*AutoConfiguration`。

### 依赖约定

| 允许 | 禁止 |
|------|------|
| `deadman-common`、`deadman-core` | 依赖 `deadman-system`（除特殊桥接） |
| 依赖其他插件（如 `storage-local` → `file`） | 插件内直接写管理端 JWT 逻辑 |
| 依赖 `deadman-security`（登录 SPI、OAuth 注入 SPI） | 与 `system` / `security` 形成环依赖 |
| 通过 SPI 与 **component** 桥接（如 wechat 手机号绑定） | 插件硬依赖 component 业务类 |

业务适配（如管理端 WebSocket 鉴权）放在 `deadman-app` 或对应 **component** 中实现 SPI。

---

## deadman-plugin-websocket

基于 Spring WebSocket 的消息通道插件。

| 能力 | 说明 |
|------|------|
| 消息通道 | 业务侧通过 `MessageChannelFactory` 声明 Bean，按用户体系隔离 |
| 统一负载体 | `WsMessage`，通道可继承扩展 |
| 调度器 | `MessageDispatcher`：持久化 → 投递 → 重试 |
| 持久化 | 表 `plugin_ws_message`（含于主库 `schema.sql`） |
| 拦截器 | `MessageDeliveryInterceptor`，投递成功或最终失败后回调 |

**配置示例：**

```yaml
deadman:
  plugin:
    websocket:
      enabled: true
      endpoint-path: /ws
      dispatch:
        max-retry: 3
        retry-interval: 30s
        batch-size: 50
```

**连接地址：**

```
ws://{host}:{port}/ws/{channelCode}?token={jwt}
```

管理端示例：`/ws/inbox?token=...`（实现 `WebSocketAuthenticator`，参考 `deadman-app` 中 `AdminJwtWebSocketAuthenticator`）。

**注册通道：**

```java
@Configuration
public class WebSocketChannelConfiguration {

    @Bean
    MessageChannel inboxMessageChannel(MessageChannelFactory factory) {
        return factory.create("inbox", "站内信收件箱");
    }
}
```

**发送消息：**

```java
@Autowired
@Qualifier("inboxMessageChannel")
private MessageChannel inboxMessageChannel;

inboxMessageChannel.dispatch("NEW_MESSAGE", String.valueOf(userId), Map.of("notificationId", id));
```

**SPI：** `WebSocketAuthenticator`、`MessageTransport`、`MessageDeliveryInterceptor`

---

## deadman-plugin-wechat

微信小程序能力，**与具体用户体系解耦**：仅依赖 `deadman-security` 的登录 Provider 框架与 OAuth 注入 SPI；与 `deadman-component-client` 等业务组件通过 **桥接层** 在 app 中组装。

| 能力 | 说明 |
|------|------|
| 小程序登录 | 实现 `LoginProvider`，按 `login-bindings` 可同时注册到多个用户体系组 |
| 手机号绑定 SPI | `WechatPhoneBindingHandler`，由用户体系组件实现（如 client 桥接） |
| OAuth 用户注入 | 通过 `OAuthLoginUserService`（security SPI），由目标用户体系提供实现 |

### 多端登录（login-bindings）

每项 binding 会动态注册一个独立的微信登录 Provider，endpoint 由目标组的 `authBasePath` + 路径段决定：

```yaml
deadman:
  plugin:
    wechat-miniprogram:
      enabled: true
      app-id: ${DEADMAN_WECHAT_MINIPROGRAM_APP_ID}
      app-secret: ${DEADMAN_WECHAT_MINIPROGRAM_APP_SECRET}
      login-bindings:
        - group-id: client
        - group-id: admin          # 可选：管理端也支持微信登录
          login-path-segment: wechat-miniprogram   # 可选，默认 wechat-miniprogram
```

| group-id | 登录 endpoint（默认路径段） |
|----------|----------------------------|
| `client` | `POST /client/api/auth/login/wechat-miniprogram` |
| `admin` | `POST /api/auth/wechat-miniprogram`（需 admin 组实现 `OAuthLoginUserService`） |

### 与 client 组件桥接

wechat 插件**不依赖** client 模块。在 `deadman-app` 同时引入两者时，client 自动装配桥接：

| 桥接类 | 职责 |
|--------|------|
| `ClientOAuthLoginUserService` | 实现 `OAuthLoginUserService`，微信登录注入 client 用户 |
| `ClientWechatPhoneBindingHandler` | 实现 `WechatPhoneBindingHandler`，绑定手机号到 client 账号 |
| `ClientWechatMiniprogramController` | `POST /client/api/wechat-miniprogram/phone/bind` |

桥接开关：`deadman.component.client.wechat.enabled`（默认 `true`，需 classpath 同时存在 wechat 插件）。

### 扩展新用户体系

1. 实现 `LoginProviderGroupContributor` 注册 API 前缀与认证路径
2. 实现 `OAuthLoginUserService`（`loginGroupId` 与组一致）
3. （可选）实现 `WechatPhoneBindingHandler`
4. 在 `login-bindings` 中增加 `group-id: {your-group}`

无需修改 wechat 插件源码。

**依赖：** `deadman-common`、`deadman-core`、`deadman-security`

**OpenAPI：** 手机号绑定接口见 [ClientWechatMiniprogramController](../doc/deadman-plugin-wechat/WechatMiniprogramController.yaml)（由 client 桥接提供）

---

## deadman-plugin-excel

基于 **EasyExcel 4.x** 的导入导出工具包，注入 `DeadExcelService` 使用。

| 能力 | 说明 |
|------|------|
| 导入 | `importList(InputStream, Class<T>)`，支持 POJO / Record |
| 导出 | `export(OutputStream, Class<T>, List<T>)` |
| 列注解 | `@DeadExcelColumn` 定义表头、列宽、日期格式 |
| 自定义列 | `DeadExcelColumns` / `DeadExcelExporter` 链式裁剪、重命名、计算列 |

**配置示例：**

```yaml
deadman:
  plugin:
    excel:
      enabled: true
```

**使用示例：**

```java
@Autowired
private DeadExcelService deadExcelService;

// 导入
List<UserRow> rows = deadExcelService.importList(inputStream, UserRow.class);

// 导出（自定义列）
DeadExcelExporter.of(deadExcelService, UserRow.class)
    .data(users)
    .sheetName("用户列表")
    .columns(c -> c.only("userCode", "nickname").header("userCode", "用户编码"))
    .write(response.getOutputStream());
```

**依赖：** 仅 `deadman-common`（最轻量插件）

---

## deadman-plugin-file

文件上传、下载与元数据管理；存储后端通过 **`FileStorageProvider` SPI** 插件化扩展。

| 能力 | 说明 |
|------|------|
| 上传 | `POST /api/files/upload`（multipart） |
| 下载 | `GET /api/files/{id}/download` |
| 元数据 | 表 `plugin_file_metadata` |
| Provider 管理 | `FileStorageProviderManager` 聚合所有 Provider Bean |

**配置示例：**

```yaml
deadman:
  plugin:
    file:
      enabled: true
      default-provider: local
      max-file-size: 10MB
```

**DDL：** `src/main/resources/db/file/schema.sql`（生产需单独执行）

**权限码：** `file:upload`、`file:download`、`file:read`、`file:delete`

**SPI：**

```java
public interface FileStorageProvider {
    String providerId();
    StoredFileRef store(FileStorageUploadContext context);
    InputStream open(StoredFileRef ref);
    void delete(StoredFileRef ref);
    Optional<String> publicAccessUrl(StoredFileRef ref);
}
```

---

## deadman-plugin-storage-local

`FileStorageProvider` 的默认实现：文件落盘至本地目录，并映射 **`GET /files/**`** 公开直链。

| 能力 | 说明 |
|------|------|
| Provider ID | `local` |
| 存储路径 | `{base-path}/{bizType}/{yyyy/MM/dd}/{uuid}.ext` |
| 直链 | `accessUrl` 如 `/files/avatar/2026/06/07/xxx.png` |
| 静态映射 | `LocalStorageWebMvcConfig` + 匿名 Security 链 |

**配置示例：**

```yaml
deadman:
  plugin:
    storage-local:
      enabled: true
      base-path: ${FILE_STORAGE_PATH:./data/files}
      public-url-prefix: /files
```

**依赖：** `deadman-plugin-file`（须同时引入 file 插件）

扩展 OSS/S3：新建 `deadman-plugin-storage-oss` 等模块，实现 `FileStorageProvider` 并注册 Bean，将 `deadman.plugin.file.default-provider` 改为对应 ID。

---

## 新增插件规范

1. 在 `plugins/deadman-plugin-{name}/` 下新建子模块，父 POM 为根项目 `deadman-admin`。
2. 包根：`com.mtfm.deadman.plugin.{name}`（子域可再分包，如 `wechat.miniprogram`）。
3. 在根 `pom.xml` 的 `<modules>` 与 `<dependencyManagement>` 登记 artifact。
4. 提供 `Deadman{Name}PluginAutoConfiguration` + `AutoConfiguration.imports`。
5. 配置类：`{Name}PluginProperties`，前缀 `deadman.plugin.{kebab-name}`。
6. 不反向依赖 `deadman-system`；鉴权、RBAC 桥接放在 `deadman-app` 或 **components**。
7. 若有独立表，DDL 放在 `src/main/resources/db/{name}/schema.sql`，插件表名建议 `plugin_{name}_*`。
8. 有 HTTP 接口时，在 [doc/](../doc/README.md) 下补充 OpenAPI YAML。

### 插件 vs 组件

| | **Plugin** | **Component** |
|---|------------|---------------|
| 定位 | 工具、基础设施、可替换实现 | 完整业务域（独立 API 前缀、用户体系可选） |
| 开关 | `deadman.plugin.*` | `deadman.component.*` |
| 目录 | `plugins/` | `components/` |
| 组件目录 | 一般不注册 | 注册 `DeadmanComponentDescriptor` |

详见 [components/README.md](../components/README.md)。
