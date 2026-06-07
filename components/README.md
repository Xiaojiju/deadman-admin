# Deadman 组件目录

本目录存放**可插拔业务组件**：具备独立 API 前缀、可选的独立用户/认证体系，并在核心组件注册表（`GET /api/components`）中登记描述信息。

与 [plugins/](../plugins/README.md) 的区别：组件面向完整业务域（如 C 端用户体系），插件面向工具或基础设施扩展（如 Excel、文件存储 Provider）。

## 组件一览

| 模块 | 配置前缀 | API 前缀 | 说明 | OpenAPI |
|------|----------|----------|------|---------|
| [deadman-component-client](#deadman-component-client) | `deadman.component.client` | `/client/api` | 用户端独立 JWT、注册登录、管理端操作用户 | [doc/deadman-component-client/](../doc/deadman-component-client/) |

## 插拔方式

1. 在根 `pom.xml` 的 `<modules>` 与 `<dependencyManagement>` 中登记（已有组件可跳过）。
2. 在 `deadman-app/pom.xml` 中引入依赖：

   ```xml
   <dependency>
       <groupId>com.mtfm</groupId>
       <artifactId>deadman-component-client</artifactId>
   </dependency>
   ```

3. 配置并开关：`deadman.component.<name>.enabled`（环境变量 `DEADMAN_COMPONENT_<NAME>_ENABLED`）。
4. 执行组件 DDL（若有），见各组件 `src/main/resources/db/`。
5. 移除依赖或 `enabled: false` 即可停用。

各组件通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 自动装配，并贡献 `DeadmanComponentDescriptor` Bean 供 `DeadmanComponentRegistry` 收录。

### 依赖约定

| 允许 | 禁止 |
|------|------|
| `deadman-common`、`deadman-core` | 依赖 `deadman-system` |
| `deadman-security` | 登录 Provider 框架、`PermissionContributor`、OAuth 注入 SPI |
| optional 依赖 `deadman-plugin-wechat`（桥接层） | 组件依赖 `security` 的业务实现类（仅 SPI 接口） |
| 被 **plugin** 通过 SPI 桥接（如 wechat 手机号绑定） | 与管理端用户表混用 |

---

## deadman-component-client

独立**用户端（C 端）**用户体系：与管理系统 `user_base` 完全隔离，使用独立 JWT（`realm=CLIENT`）与独立 Security 过滤链（`@Order(20)`，`/client/api/**`）。

登录认证统一由 **`deadman-security` 的 `LoginProviderGroupManager`** 管理：client 组件通过 `ClientLoginProviderGroupContributor` 注册 endpoint 前缀，不再自建 Provider 管理器。

### 能力概览

| 能力 | 说明 |
|------|------|
| 注册 | `POST /client/api/auth/register` |
| 登录 | 多 Provider：`POST /client/api/auth/login/{provider}`（如 `password`、`wechat-miniprogram`） |
| 个人中心 | `GET/PUT /client/api/users/me` |
| 管理端操作 | `GET /api/client-users` 等（走管理端 JWT + 权限码） |
| 权限贡献 | `ClientPermissionContributor` → `client-user:*` |
| 组件注册 | `code=client`，`apiPrefix=/client/api` |

### 数据表

DDL：`src/main/resources/db/client/schema.sql`（生产需单独执行）

| 表 | 说明 |
|----|------|
| `client_user_base` | 用户基础信息 |
| `client_user_account` | 登录账号（用户名、手机、OAuth 等） |
| `client_user_password` | 密码哈希 |

集成测试使用 `schema-h2.sql`，已在 `deadman-app` 的 `application-test.yaml` 中引用。

### 配置示例

```yaml
deadman:
  component:
    client:
      enabled: true
      jwt:
        secret: ${DEADMAN_CLIENT_JWT_SECRET}
        expiration-ms: 604800000
      user:
        user-code-prefix: CL
      security:
        multi-session-enabled: true
      wechat:
        enabled: true   # 与 wechat 插件同时引入时，自动桥接手机号绑定等能力
```

环境变量见根目录 [.env.example](../.env.example)。

### 认证说明

- **用户端请求**：`Authorization: Bearer {clientAccessToken}`，由 `ClientJwtAuthenticationFilter` 解析。
- **管理端操作用户端用户**：仍使用管理端 Token（`/api/client-users/**`），权限码 `client-user:list:read` 等。
- 注册与各登录端点为用户端 Security 链放行路径，无需 Token。

### 主要接口

| 方法 | 路径 | 认证 |
|------|------|------|
| POST | `/client/api/auth/register` | 公开 |
| POST | `/client/api/auth/login/password` | 公开 |
| POST | `/client/api/auth/login/wechat-miniprogram` | 公开（需 wechat 插件 + login-bindings 含 client） |
| POST | `/client/api/wechat-miniprogram/phone/bind` | 用户端 JWT（client-wechat 桥接） |
| GET | `/client/api/users/me` | 用户端 JWT |
| PUT | `/client/api/users/me` | 用户端 JWT |
| GET | `/api/client-users` | 管理端 JWT + `client-user:list:read` |
| PUT | `/api/client-users/{id}/disable` | 管理端 JWT + `client-user:update` |
| DELETE | `/api/client-users/{id}` | 管理端 JWT + `client-user:delete` |

完整定义见 [doc/deadman-component-client/](../doc/deadman-component-client/)。

### SPI 扩展

组件通过 Spring Bean 聚合 SPI，供插件或业务模块扩展登录与用户开通逻辑。登录 Provider 归属 **`deadman-security` 统一框架**（`LoginProvider` / `LoginProviderGroupManager`）。

#### ClientLoginProvider

继承 security 的 `LoginProvider`，固定 `loginGroupId = "client"`。每种登录方式实现一个 Bean，由 `LoginProviderGroupManager` 聚合并自动注册独立登录 Filter。

```java
@Component
public class PasswordClientLoginProvider implements ClientLoginProvider {

    @Override
    public String providerId() {
        return "password";
    }

    @Override
    public Authentication createAuthenticationRequest(HttpServletRequest request) {
        // 从 JSON 体解析用户名密码
    }

    // supports / authenticate ...
}
```

- 完整路径：`{auth.basePath}/login/{loginPathSegment}`，默认 `/client/api/auth/login/{providerId}`。
- 微信登录由 `deadman-plugin-wechat` 按 `login-bindings` 动态注册，无需实现 `ClientLoginProvider`。

#### ClientUserProvisioner / ClientOAuthLoginUserService

- `ClientUserProvisioner`：首次 OAuth 登录时创建用户端账号（无本地用户则开通）。
- `ClientOAuthLoginUserService`：实现 security 的 `OAuthLoginUserService`，供微信等 OAuth 插件按 `loginGroupId=client` 注入用户。

#### ClientLoginFailureCallback

登录失败后的扩展回调（审计、风控等），可注册多个，由 `CompositeClientLoginFailureCallback` 聚合。

#### 与 wechat 插件桥接

client 对 wechat 为 **optional 依赖**，仅在两者同时引入时装配 `ClientWechatIntegrationAutoConfiguration`：

| 类 | SPI / 职责 |
|----|------------|
| `ClientOAuthLoginUserService` | `OAuthLoginUserService` |
| `ClientWechatPhoneBindingHandler` | `WechatPhoneBindingHandler` |
| `ClientWechatMiniprogramController` | 手机号绑定 HTTP 接口 |

### 与管理端的关系

```
                    deadman-security
              LoginProviderGroupManager
                     /            \
            group: admin      group: client
                 │                  │
    管理系统 (user_base)    用户端 (client_user_base)
         │  管理端 JWT            │  用户端 JWT
         ▼                        ▼
    /api/**                 /client/api/**
         │                        │
         │    /api/client-users   │  wechat 桥接（optional）
         └──────── 管理 C 端 ──────┘    /client/api/wechat-miniprogram/**
```

---

## 新增组件规范

1. 在 `components/deadman-component-{name}/` 下新建子模块，父 POM 为根项目 `deadman-admin`。
2. 包根：`com.mtfm.deadman.component.{name}`。
3. 在根 `pom.xml` 登记 module 与 `dependencyManagement`；在 `deadman-app/pom.xml` 按需引入。
4. 提供：
   - `Deadman{Name}ComponentAutoConfiguration`
   - `{Name}ComponentProperties`，前缀 `deadman.component.{kebab-name}`
   - `{Name}ComponentRegistration` → `DeadmanComponentDescriptor` Bean
5. 若需独立 Security 链：单独 `@Configuration` + `@Order`，`securityMatcher` 限定组件 API 前缀；登录 Provider 通过 `LoginProviderGroupContributor` 注册到 security 统一管理器。
6. 若需管理端权限：实现 `PermissionContributor`（依赖 `deadman-security`）。
7. 若需 OAuth 登录：实现 `OAuthLoginUserService`（`loginGroupId` 与组一致）。
8. DDL 放在 `src/main/resources/db/{name}/schema.sql`，表名建议 `component_{name}_*` 或业务语义前缀（如 `client_user_*`）。
9. 在 [doc/](../doc/README.md) 下按 Controller 补充 OpenAPI YAML。
10. **禁止**依赖 `deadman-system`；跨体系能力通过 SPI 或 `deadman-app` 桥接。

### 组件 vs 插件选型

| 场景 | 建议 |
|------|------|
| Excel 导入、文件存储 Provider、WebSocket 通道 | **Plugin** |
| 独立 App 用户体系、独立 API 域、需出现在组件目录 | **Component** |
| 与管理端深度集成、默认随 app 装配 | 一级模块（如 `deadman-notification`） |
