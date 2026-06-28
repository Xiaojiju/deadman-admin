# OpenAPI 接口文档

静态 OpenAPI 3.0 YAML，与源码 Controller 一一对应，不依赖 SpringDoc / Swagger UI。

## 目录

| 模块 | 文件 | Controller |
| ------ | ------ | ------------ |
| deadman-security | [AuthController.yaml](deadman-security/AuthController.yaml) | 注册、登录、改密、当前权限 |
| deadman-security | [JwtTokenRefresh.md](deadman-security/JwtTokenRefresh.md) | JWT 双令牌与无感刷新（后端） |
| deadman-security | [JwtTokenRefresh-Frontend.md](deadman-security/JwtTokenRefresh-Frontend.md) | JWT 无感刷新（前端对接） |
| deadman-security | [UserController.yaml](deadman-security/UserController.yaml) | 当前用户资料 |
| deadman-system | [UserAdminController.yaml](deadman-system/UserAdminController.yaml) | 管理端用户 CRUD |
| deadman-system | [RoleController.yaml](deadman-system/RoleController.yaml) | 角色与权限分配 |
| deadman-system | [PermissionController.yaml](deadman-system/PermissionController.yaml) | 权限码目录 |
| deadman-system | [DepartmentController.yaml](deadman-system/DepartmentController.yaml) | 部门管理 |
| deadman-system | [PositionController.yaml](deadman-system/PositionController.yaml) | 职位管理 |
| deadman-notification | [NotificationSendController.yaml](deadman-notification/NotificationSendController.yaml) | 站内信发送与已发列表 |
| deadman-notification | [NotificationInboxController.yaml](deadman-notification/NotificationInboxController.yaml) | 站内信收件箱与已读 |
| deadman-core | [DeadmanComponentController.yaml](deadman-core/DeadmanComponentController.yaml) | 已装配组件目录 |
| deadman-component-client | [ClientAuthController.yaml](deadman-component-client/ClientAuthController.yaml) | 用户端注册与登录 |
| deadman-support-client-wechat | [ClientWechatAuth.md](deadman-support-client-wechat/ClientWechatAuth.md) | 用户端微信小程序 OAuth（后端） |
| deadman-support-client-wechat | [ClientWechatAuth-Frontend.md](deadman-support-client-wechat/ClientWechatAuth-Frontend.md) | 用户端微信小程序 OAuth（前端对接） |
| deadman-support-client-wechat | [ClientWechatWebAuth.md](deadman-support-client-wechat/ClientWechatWebAuth.md) | 用户端微信网页扫码 OAuth（后端） |
| deadman-support-client-wechat | [ClientWechatWebAuth-Frontend.md](deadman-support-client-wechat/ClientWechatWebAuth-Frontend.md) | 用户端微信网页扫码 OAuth（前端对接） |
| deadman-support-wechat | [AdminWechatWebAuth.md](deadman-support-wechat/AdminWechatWebAuth.md) | 管理端微信网页扫码 OAuth（后端） |
| deadman-support-wechat | [AdminWechatWebAuth-Frontend.md](deadman-support-wechat/AdminWechatWebAuth-Frontend.md) | 管理端微信网页扫码 OAuth（前端对接） |
| deadman-component-client | [ClientUserController.yaml](deadman-component-client/ClientUserController.yaml) | 用户端当前用户资料 |
| deadman-component-client | [ClientUserAdminController.yaml](deadman-component-client/ClientUserAdminController.yaml) | 管理端操作用户端用户 |
| deadman-component-open-auth | [OpenAuthController.yaml](deadman-component-open-auth/OpenAuthController.yaml) | 开放应用管理与 OAuth Token 兑换 |
| deadman-component-open-auth | [OpenAuth.md](deadman-component-open-auth/OpenAuth.md) | 开放授权对接指南（第三方/Python） |
| deadman-support-client-open-auth | [ClientOpenAuthController.yaml](deadman-support-client-open-auth/ClientOpenAuthController.yaml) | 用户端申请 auth_code |
| deadman-plugin-wechat | [WechatMiniprogramController.yaml](deadman-plugin-wechat/WechatMiniprogramController.yaml) | 微信小程序手机号绑定 |
| deadman-plugin-wechat | [WechatWebLogin.md](deadman-plugin-wechat/WechatWebLogin.md) | 微信网页扫码登录（后端） |
| deadman-plugin-wechat | [WechatLogin.yaml](deadman-plugin-wechat/WechatLogin.yaml) | 微信登录统一公开 API |
| deadman-plugin-wechat | [WechatLoginProviderRegistration.md](deadman-plugin-wechat/WechatLoginProviderRegistration.md) | 微信 LoginProvider 注册与覆盖机制 |
| deadman-extension-file | [FileController.yaml](deadman-plugin-file/FileController.yaml) | 文件上传、下载与元数据 |
| deadman-plugin-data-scope | [UserDataScopeAdminController.yaml](deadman-plugin-data-scope/UserDataScopeAdminController.yaml) | 用户数据范围查询与分配 |

公共片段：[\_shared/components.yaml](_shared/components.yaml)（管理端/用户端 JWT、`Result`、`PageParam` / `PageVO`、通用 401/403）。

分页约定：请求继承 `com.mtfm.deadman.common.page.PageParam`（`current` 默认 1、`size` 默认 10、最大 100）；响应使用 `PageVO<T>`。

## 使用

1. 在 Apifox / Postman 中导入单个 Controller YAML，或合并多个文件。
2. 管理端接口（`/api/**`，不含 `/client/api`）使用管理端 Token：`Authorization: Bearer {adminAccessToken}`（`POST /api/auth/login` 获取，`POST /api/auth/refresh` 刷新）。
3. 用户端接口（`/client/api/**`）使用用户端 Token：`Authorization: Bearer {clientAccessToken}`（登录接口获取，`POST /client/api/auth/refresh` 刷新）。
4. 公开接口：`POST /api/auth/register`、`POST /api/auth/login`、`GET /api/components`、用户端注册/登录路径、`POST /open-api/oauth/token`（凭 client_secret 鉴权，非 JWT）无需 Token；本地存储直链 `GET /files/**` 无需 Token（需启用 `deadman-plugin-storage-local`）。

默认服务地址：`http://localhost:8080`（见各文件 `servers`）。
