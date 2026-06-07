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
| `deadman-security`（optional，用于 `PermissionContributor`） | 组件依赖 `security` 的业务实现类（仅 SPI 接口） |
| 被 **plugin** 依赖（如 wechat → client） | 与管理端用户表混用 |

---

## deadman-component-client

独立**用户端（C 端）**用户体系：与管理系统 `user_base` 完全隔离，使用独立 JWT（`realm=CLIENT`）与独立 Security 过滤链（`@Order(20)`，`/client/api/**`）。

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
| GET | `/client/api/users/me` | 用户端 JWT |
| PUT | `/client/api/users/me` | 用户端 JWT |
| GET | `/api/client-users` | 管理端 JWT + `client-user:list:read` |
| PUT | `/api/client-users/{id}/disable` | 管理端 JWT + `client-user:update` |
| DELETE | `/api/client-users/{id}` | 管理端 JWT + `client-user:delete` |

完整定义见 [doc/deadman-component-client/](../doc/deadman-component-client/)。

### SPI 扩展

组件通过 Spring Bean 聚合 SPI，供插件或业务模块扩展登录与用户开通逻辑。

#### ClientLoginProvider

每种登录方式实现一个 Bean，自动注册独立登录 Filter。

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
- 微信登录由 `deadman-plugin-wechat` 提供 `WechatMiniprogramLoginProvider`。

#### ClientUserProvisioner

首次通过第三方登录创建用户端账号时调用（无本地用户则开通）。

#### ClientLoginFailureCallback

登录失败后的扩展回调（审计、风控等），可注册多个，由 `CompositeClientLoginFailureCallback` 聚合。

### 与管理端的关系

```
管理系统用户 (user_base)          用户端用户 (client_user_base)
        │                                    │
        │  管理端 JWT                         │  用户端 JWT
        ▼                                    ▼
   /api/**                           /client/api/**
        │                                    │
        └──────── /api/client-users ─────────┘
                  （管理端查/管 C 端用户）
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
5. 若需独立 Security 链：单独 `@Configuration` + `@Order`，`securityMatcher` 限定组件 API 前缀。
6. 若需管理端权限：实现 `PermissionContributor`（依赖 `deadman-security` 中接口，optional 依赖）。
7. DDL 放在 `src/main/resources/db/{name}/schema.sql`，表名建议 `component_{name}_*` 或业务语义前缀（如 `client_user_*`）。
8. 在 [doc/](../doc/README.md) 下按 Controller 补充 OpenAPI YAML。
9. **禁止**依赖 `deadman-system`；跨体系能力通过 SPI 或 `deadman-app` 桥接。

### 组件 vs 插件选型

| 场景 | 建议 |
|------|------|
| Excel 导入、文件存储 Provider、WebSocket 通道 | **Plugin** |
| 独立 App 用户体系、独立 API 域、需出现在组件目录 | **Component** |
| 与管理端深度集成、默认随 app 装配 | 一级模块（如 `deadman-notification`） |
