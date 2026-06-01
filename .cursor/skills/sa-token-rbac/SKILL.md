---
name: sa-token-rbac
description: SDP 基于 Sa-Token + Redis 的认证与 RBAC（用户-角色-菜单/权限）鉴权约定。在新增后端接口需要加权限校验、定义权限点、改动 RbacPermissionService/StpInterfaceImpl、或在前端用 v-perm 指令/权限 store 控制按钮可见性时使用。
---

# SDP 认证与 RBAC 鉴权约定

认证用 **Sa-Token + Redis**（Opaque Token，非 JWT），鉴权用 **用户-角色-菜单** RBAC。权限判定**必须在服务端落地**，前端只改善体验（隐藏按钮），绝不作为安全边界。背景见 `back-end/sdp-admin/DESIGN_UTF8.md` 第 2~3 节、`docs/sdp-auth-rbac-design.md`。

## 权限模型

```
sys_user ── sys_user_role ── sys_role ── sys_role_menu ── sys_menu(.perms)
```

- 权限码挂在 `sys_menu.perms`（一菜单可逗号分隔多个），用户的权限 = 其角色关联菜单的 perms 并集。
- 后端权限计算入口：`org.nmgyj.system.service.RbacPermissionService`。
  - `listPermissionCodes(userId)`：超管返回 `["*"]`；否则取启用菜单(`status==1`)的 perms。
  - `listRoleCodes(userId)`、`collectAllowedMenuIdsWithAncestors(userId)`（菜单树含祖先）。
- Sa-Token 通过 `org.nmgyj.system.satoken.StpInterfaceImpl` 桥接上面服务。
- **超级管理员**：角色码 `SUPER_ADMIN`（别名 `SUPERADMIN`/`SUPER-ADMIN`/`ROOT`，或角色名"超级管理员"）拥有 `*` 全权限。判断用 `RbacPermissionService.isSuperAdmin`，勿另写。

## 权限码命名规范

固定三段 `模块:资源:操作`，小写冒号分隔：

- 列表/详情：`system:role:list`
- 增删改：`system:role:edit`
- 特殊动作：`system:role:assign`

业务模块同理，如 `business:order:list` / `business:order:edit`。新增菜单时把权限码写进 `sys_menu.perms`，并在种子 SQL（`db/seed-dm.sql`、`db/rbac-dm.sql`）中分配给相应角色。

## 后端：接口加鉴权

Controller 方法上加 Sa-Token 注解，权限码与上面命名一致：

```java
@GetMapping
@SaCheckPermission("system:role:list")
public ApiResponse<List<SysRole>> list() { ... }

@PostMapping
@SaCheckPermission("system:role:edit")
public ApiResponse<Void> create(@RequestBody SysRole body) { ... }
```

约定：

- 读接口用 `:list`，写接口（POST/PUT/DELETE）用 `:edit`，特殊用专属码。
- 同一资源的列表/详情/已分配查询通常共用 `:list`。
- 鉴权失败由 `GlobalExceptionHandler` 统一转 `ApiResponse.fail(403,...)`，**不要自己 catch** `NotPermissionException`。
- 登录态 `StpUtil.login(userId)`；当前用户 `StpUtil.getLoginId()`。token 配置见 `application-dev.yml`（`token-name=satoken`、uuid 风格、`is-share=false`）。

## 前端：按钮级权限

权限码在登录后由 `usePermissionStore().loadFromApi()` 拉取（`GET /api/admin/.../my-permissions`，见 `api/admin.ts` 的 `fetchMyPermissions`），存入 Pinia。

用 `v-perm` 指令控制元素显隐（不可见即 `display:none`）：

```vue
<el-button v-perm="'system:role:edit'" @click="openCreate">新增</el-button>
<el-button v-perm="['system:role:edit','system:role:assign']" @click="...">操作</el-button>
```

- 字符串 = 需该权限；数组 = 拥有**任一**即可（`hasAnyPerm`）。
- store 提供 `hasPerm` / `hasAnyPerm` / `hasAllPerm`；持有 `*` 视为全通过。
- 前端权限码必须与后端 `@SaCheckPermission` 的码**逐字一致**。
- 前端隐藏 ≠ 安全：后端必须有对应 `@SaCheckPermission`，否则形同虚设。

## 新增受控功能的完整链路

1. 后端 Controller 方法加 `@SaCheckPermission("模块:资源:操作")`。
2. `sys_menu` 新增/更新菜单行，`perms` 填该权限码。
3. 种子 SQL 把菜单分配给角色（`sys_role_menu`），幂等插入。
4. 前端对应按钮加 `v-perm="'模块:资源:操作'"`。
5. 验证：无权限用户调接口返回 403，按钮不可见；超管全部可见可用。
