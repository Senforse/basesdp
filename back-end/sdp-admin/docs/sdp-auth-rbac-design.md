# SDP 认证与 RBAC 设计（Sa-Token + Redis）

## 架构决策

- **Sa-Token**：会话与权限校验；Token 持久化到 **Redis**（`sa-token-redis-jackson` + Spring Data Redis），配置见 `application-dev.yml` 的 `spring.data.redis` 与 `sa-token.*`。
- **Spring Security**：移除 **spring-boot-starter-security** 过滤器链（原先均为 `permitAll`，未提供实质保护）。保留 **`spring-security-crypto`** 仅用于 **BCrypt** 密码哈希，避免与 Sa-Token 双重认证模型纠缠。
- **认证边界**：`SaInterceptor` 对 `/api/**` 做登录校验（放行 `/api/auth/login`、`OPTIONS`、错误页与 Actuator）；管理接口另加 `@SaCheckRole("super_admin")`。
- **CSRF**：前后端分离 REST，禁用表单 CSRF；依赖 Token 头与会话校验。
- **CORS**：通过 `WebMvcConfigurer#addCorsMappings` 统一配置（不再依赖 Security 的 CORS 过滤器）。

## 数据模型（达梦 DM，可类推 MySQL）

| 表名 | 说明 |
|------|------|
| `sys_org` | 组织树：`parent_id`、`org_name`、`org_code`、`sort_order`、`status` |
| `sys_role` | 角色：`role_code`（唯一）、`role_name`、`status`；内置 `super_admin` |
| `sys_menu` | 菜单：`parent_id`、`path`、`component`（前端路由键）、`perms`（权限串）、`menu_type`（0目录 1菜单 2按钮）、`icon`、`sort_order`、`status` |
| `sys_user` | 用户：`username`、`password`（BCrypt）、`display_name`、`status`、`org_id` |
| `sys_user_role` | 用户-角色（复合主键） |
| `sys_role_menu` | 角色-菜单 |
| `sys_role_org` | 角色-组织数据范围（预留，便于按组织授权） |
| `sys_login_log` | 登录审计：`log_type`（1登录 2登出）、`login_status`、`login_ip`、`user_agent`、`remark` |

**权限模型**：登录用户若为 `super_admin`，Sa-Token 权限列表返回 `*`（放行所有 `StpUtil.checkPermission`）；否则聚合 `sys_role_menu → sys_menu.perms`。

## API 一览（前缀 `/api`）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/login` | 登录，返回 token、`tokenHeaderName=satoken` |
| POST | `/auth/logout` | 登出 |
| GET | `/auth/menus` | 当前用户可见菜单树（路由） |
| GET | `/auth/profile` | 当前用户档案 |
| CRUD | `/admin/orgs/**` | 组织管理（超管） |
| CRUD | `/admin/menus/**` | 菜单管理（超管） |
| CRUD | `/admin/roles/**` | 角色及菜单绑定（超管） |
| CRUD | `/admin/users/**` | 用户及角色绑定（超管） |

## 审计

- **失败登录**：仍在 `AuthService` 写 `sys_login_log`（登录前无会话，便于带客户端 IP）。
- **成功登录 / 登出 / 被踢下线**：`SaTokenListener` + `LoginAuditContextFilter`（ThreadLocal 携带 IP、UA）写入 `sys_login_log`。

## 前端约定

- 请求头：`satoken: <token>`（与 `sa-token.token-name` 一致）。
- 菜单：`GET /auth/menus` 返回树，`component` 与 Vue 侧 `dynamicRouteMap` 键一致。

## SQL 脚本

- `sdp-server/src/main/resources/db/schema-dm.sql`：建表（达梦语法）。
- `sdp-server/src/main/resources/db/seed-dm.sql`：种子数据（默认账号见脚本注释）。

假设：**新建库或空库** 执行；若已有 `sys_user` 使用 `dept_id` 等字段，需自行迁移至 `org_id` 与新表结构。
