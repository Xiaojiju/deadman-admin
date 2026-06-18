# deadman-admin

Deadman Switch 管理后台（Spring Boot 4 + Java 21），Maven 多模块结构，核心能力与插件/组件可按需组装。

## 技术栈

| 类别 | 选型 |
|------|------|
| 运行时 | Java 21（虚拟线程） |
| 框架 | Spring Boot 4.0.6、Spring Security、JWT 无状态 |
| 持久化 | MyBatis-Plus 3.5.x、MySQL 8+（测试 H2 MySQL 模式） |
| 缓存 | Redis（Spring Cache） |
| 序列化 | Jackson 3 |

编码规范与分层约定见 [.cursor/rules/java.mdc](.cursor/rules/java.mdc)。

## 模块结构

```
deadman-admin/                          # 父 POM（依赖版本管理）
├── deadman-common/                     # 公共：Result、异常、常量、跨模块 SPI
├── deadman-core/                       # 基础设施：Redis、MyBatis、Jackson、组件注册表
├── deadman-system/                     # 管理端用户与 RBAC 领域
├── deadman-notification/               # 站内信与 WebSocket 收件箱推送
├── deadman-security/                   # 认证授权、JWT、权限注册 SPI
├── plugins/                            # 可插拔插件（见 plugins/README.md）
│   ├── deadman-plugin-websocket/       # WebSocket 消息通道
│   ├── deadman-plugin-wechat/          # 微信小程序登录与手机号
│   ├── deadman-plugin-excel/           # EasyExcel 导入导出工具包
│   ├── deadman-plugin-file/            # 文件上传下载与存储 SPI
│   └── deadman-plugin-storage-local/   # 本地磁盘存储 Provider
├── components/                         # 可插拔业务组件（见 components/README.md）
│   └── deadman-component-client/       # 用户端（独立 JWT，/client/api）
└── deadman-app/                        # 默认组装与启动入口
```

### 依赖方向

```
common ← core ← system ← security ← app
                    ↑
         notification / plugins / components（经 app 组装，禁止环依赖）
```

| 模块 | 职责 |
|------|------|
| **common** | 统一响应、错误码、工具、跨模块 SPI（如 `UserAuthorityCache`） |
| **core** | 可覆盖的基础设施 Bean、组件目录 `GET /api/components` |
| **system** | 用户/部门/职位/RBAC 表与业务，不依赖 security |
| **security** | 登录 Provider 统一管理、JWT、`PermissionContributor` / OAuth 注入 SPI 聚合 |
| **notification** | 站内信发送、收件箱、与 WebSocket 通道联动 |
| **plugins** | 工具或基础设施扩展，通过 SPI 接入，默认不依赖 system/security |
| **components** | 独立 API 前缀与用户体系的业务域（如用户端） |
| **app** | `application-example.yaml`（示例）、DB 主脚本、集成测试、按需引入模块 |

### 自由组装

在自定义启动模块的 `pom.xml` 中只引入需要的 artifact，例如仅管理端用户与认证：

```xml
<dependencies>
    <dependency>
        <groupId>com.mtfm</groupId>
        <artifactId>deadman-common</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mtfm</groupId>
        <artifactId>deadman-core</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mtfm</groupId>
        <artifactId>deadman-system</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mtfm</groupId>
        <artifactId>deadman-security</artifactId>
    </dependency>
</dependencies>
```

各模块通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 自动注册。在 app 中声明同名 `@Configuration` 并标注 `@Primary` 或 `@ConditionalOnMissingBean` 即可覆盖默认 Bean。

插件开关：`deadman.plugin.<name>.enabled`；组件开关：`deadman.component.<name>.enabled`。

## 环境变量

参考 [.env.example](.env.example)。常用项：

| 变量 | 说明 |
|------|------|
| `DB_*` | MySQL 连接 |
| `REDIS_*` | Redis |
| `DEADMAN_JWT_SECRET` | 管理端 JWT 密钥（≥32 字节） |
| `DEADMAN_BOOTSTRAP_*` / `DEADMAN_SUPER_ADMIN_*` | 首次启动引导超级管理员 |
| `DEADMAN_COMPONENT_CLIENT_*` | 用户端组件（独立 JWT、用户编码前缀） |
| `DEADMAN_WECHAT_MINIPROGRAM_*` | 微信小程序 AppId / Secret |
| `FILE_STORAGE_PATH` | 本地文件存储根目录（默认 `./data/files`） |
| `DEADMAN_PLUGIN_FILE_MAX_SIZE` | 单文件上传上限（默认 `10MB`） |

## 本地启动

```bash
# 1. 创建库并初始化主库表（用户、RBAC、站内信、WebSocket 消息等）
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS deadman_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p deadman_admin < deadman-app/src/main/resources/db/schema.sql

# 若从旧版（user_base.department_id 单部门模型）升级，额外执行迁移脚本：
mysql -u root -p deadman_admin < deadman-app/src/main/resources/db/migration/20260616_user_department_refactor.sql

# 2. 按需初始化组件/插件表
mysql -u root -p deadman_admin < components/deadman-component-client/src/main/resources/db/client/schema.sql
mysql -u root -p deadman_admin < plugins/deadman-plugin-file/src/main/resources/db/file/schema.sql

# 3. 复制示例配置为本地 application.yaml 并按需修改（该文件不纳入 Git）
cp deadman-app/src/main/resources/application-example.yaml deadman-app/src/main/resources/application.yaml

# 4. 配置环境变量后启动
export DEADMAN_JWT_SECRET="your-secret-at-least-32-characters-long"
export DB_PASSWORD="your_db_password"
./mvnw -pl deadman-app spring-boot:run
```

打包与运行：

```bash
./mvnw -pl deadman-app package
java -jar deadman-app/target/deadman-app-1.0.0.jar
```

测试（H2，无需 MySQL/Redis）：

```bash
./mvnw verify
```

项目根目录 `.mvn/settings.xml` 已配置阿里云 Maven 镜像，便于国内环境拉取依赖。

## API 与认证

### 多用户体系与 LoginProvider 框架

`deadman-security` 提供统一的 **LoginProvider 管理器**（`LoginProviderGroupManager`），支持在同一应用中并存多套用户体系：

| 组 ID | API 前缀 | 说明 |
|-------|----------|------|
| `admin` | `/api/**` | 管理端（`user_base`），密码登录 `POST /api/auth/login` |
| `client` | `/client/api/**` | 用户端组件（`client_user_base`），多 Provider 登录 |

各用户体系通过 `LoginProviderGroupContributor` 绑定 endpoint 前缀，通过独立 `SecurityFilterChain`（不同 `@Order`）隔离；同一 Filter 类（`ProviderLoginFilter`）按 endpoint 路由到对应组的 `AuthenticationManager`。

OAuth 类登录（如微信小程序）通过 `OAuthLoginUserService` SPI 注入目标体系，插件侧用 `login-bindings` 配置可同时服务多个组。详见 [plugins/README.md](plugins/README.md#deadman-plugin-wechat)、[components/README.md](components/README.md#deadman-component-client)。

### 路径前缀

| 前缀 | 说明 | Token |
|------|------|-------|
| `/api/**` | 管理端 REST API | 管理端 JWT（`POST /api/auth/login`） |
| `/client/api/**` | 用户端组件 API | 用户端 JWT（注册或 `/client/api/auth/login/*`，含 wechat-miniprogram） |
| `/ws/**` | WebSocket（握手可带 `?token=`） | 按通道 `WebSocketAuthenticator` |
| `/files/**` | 本地存储公开直链 | 无需 Token（需 `deadman-plugin-storage-local`） |

公开接口（无需管理端 Token）：`POST /api/auth/register`、`POST /api/auth/login`、`GET /api/components`，以及用户端注册/登录路径。

### OpenAPI 文档

静态 OpenAPI 3.0 YAML 位于 **[doc/](doc/README.md)**，按 Controller 拆分，可导入 Apifox / Postman。涵盖管理端、站内信、用户端组件、微信插件、文件管理等接口。

## 权限体系（RBAC）

- 各模块实现 `PermissionContributor`（`deadman-security`），启动时由 `PermissionRegistry` 聚合权限目录；接口使用 `@PreAuthorize("hasAuthority('权限码')")`。
- 无菜单表；角色绑定权限码字符串。
- 系统内置角色（不可删除）：
  - `SUPER_ADMIN`：拥有全部权限（不依赖 `sys_role_permission` 逐条绑定）
  - `USER`：注册默认角色
- 用户权限缓存：Redis `userAuthorities`（默认 30 分钟，角色变更通过 `UserAuthorityCache` SPI 失效）。

新增权限：在对应模块增加 `*Permissions` 常量类与 `*PermissionContributor`，发版后通过角色接口分配。

### 管理端主要接口（节选）

完整路径与请求体见 [doc/](doc/README.md)。

| 域 | 示例路径 | 权限码示例 |
|----|----------|------------|
| 认证 | `POST /api/auth/register`、`/api/auth/login` | 公开 |
| 用户 | `GET /api/users/me`、`GET /api/users` | `user:profile:read`、`user:list:read` |
| 组织 | `GET /api/departments/tree`、`/api/positions` | `dept:list:read`、`position:list:read` |
| 角色 | `GET /api/roles`、`PUT /api/roles/{id}/permissions` | `role:list:read`、`role:permission:assign` |
| 站内信 | `POST /api/notifications/send`、收件箱 | `notification:send`、`notification:inbox:read` |
| 用户端管理 | `GET /api/client-users` | `client-user:list:read` |
| 文件 | `POST /api/files/upload`、`GET /api/files/{id}/download` | `file:upload`、`file:download` |
| 组件目录 | `GET /api/components` | 公开 |

## 插件与组件速览

详细说明见 [plugins/README.md](plugins/README.md)、[components/README.md](components/README.md)。

| 模块 | 能力摘要 |
|------|----------|
| **websocket** | 多通道 WebSocket、`MessageDispatcher` 持久化与重试，表 `plugin_ws_message` |
| **wechat** | 小程序登录（`login-bindings` 多端）、`WechatPhoneBindingHandler` SPI；与 client 桥接后提供手机号绑定 |
| **excel** | 注入 `DeadExcelService`：POJO/Record 导入导出、`DeadExcelColumns` 自定义列 |
| **file** | 上传下载 API、`FileStorageProvider` SPI、元数据表 `plugin_file_metadata` |
| **storage-local** | 默认 `local` Provider，磁盘存储 + `GET /files/**` 直链 |
| **component-client** | 独立用户表与 JWT，路径 `/client/api`；`ClientLoginProvider` + OAuth/wechat 桥接 |

存储扩展：实现 `FileStorageProvider` 并注册为 Spring Bean，配置 `deadman.plugin.file.default-provider` 即可切换。

Excel 示例：

```java
@Autowired
private DeadExcelService deadExcelService;

// 导入
List<UserRow> rows = deadExcelService.importList(inputStream, UserRow.class);

// 导出（可链式自定义列）
DeadExcelExporter.of(deadExcelService, UserRow.class)
    .data(users)
    .columns(c -> c.header("userCode", "用户编码"))
    .write(outputStream);
```

## 数据模型（管理端）

- `user_base` / `user_account` / `user_password`：用户与多登录方式
- `sys_department` / `sys_position` / `sys_user_department` / `sys_user_position`：组织与职位（多部门 + 主部门）
- `sys_role` / `sys_user_role` / `sys_role_permission`：RBAC
- `sys_notification` / `sys_notification_recipient`：站内信
- `plugin_ws_message`：WebSocket 消息持久化

用户端表（`client_user_*`）与文件元数据（`plugin_file_metadata`）见各模块 `src/main/resources/db/` 下脚本。

扩展密码编码器：在 `deadman-core` 的 `PasswordEncoderRegistry` 注册并写入 `PasswordEncoderIds` 常量。

## 首次部署

1. 执行数据库脚本（见「本地启动」）。
2. 配置 `DEADMAN_SUPER_ADMIN_USERNAME`、`DEADMAN_SUPER_ADMIN_PASSWORD`（见 `.env.example`）。
3. 启动应用：若库中**尚无 SUPER_ADMIN 用户**，将自动创建管理员（仅 `SUPER_ADMIN` 角色）。
4. 生产环境务必修改默认密码与 JWT 密钥；文件存储目录需保证进程可写。
