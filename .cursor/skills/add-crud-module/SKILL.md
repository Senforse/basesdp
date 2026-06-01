---
name: add-crud-module
description: SDP 端到端新增一个业务模块（数据表 + 后端 entity/mapper/service/controller + 前端 api/view + 路由菜单 + RBAC 权限）的完整工作流。在需要"加一张业务表/新增 CRUD 模块/新建业务功能页面"时使用，覆盖在线代码生成器与手工新增两条路径。
---

# SDP 新增业务模块工作流

把一张新业务表从 0 做到可用，需贯穿：达梦建表 → 后端四件套 → 前端 api+页面 → 路由 → 菜单 → RBAC 权限分配。SDP 自带在线代码生成器覆盖大部分步骤，**优先用它**，手工只作兜底。

相关规范交叉参考同目录其它 skill：`sa-token-rbac`（权限码与鉴权）、`vue-page`（前端页面）、`backend-logging`（审计日志）、`utf8-encoding`（中文落盘）。

## 命名与约定（两条路径都遵守）

以表名 `buss_order_info` 为例（来自生成器实际产物）：

- 实体/类名：表名 PascalCase → `BussOrderInfo`、`BussOrderInfoMapper/Service/Controller`
- Java 包：`org.nmgyj.<modulePackage>`，默认 `business`（如 `org.nmgyj.order` 这种按业务再分包也可）
- 包目录固定四层：`entity` / `mapper` / `service` / `controller`，落在 `sdp-module-business/src/main/java`
- **API 路径**：`/api/admin/<表名小写带下划线>`，即 `/api/admin/buss_order_info`（注意：用下划线小写，不是 kebab）
- **权限前缀**：`business:<表名小写>`，业务模块操作码为 **`list` / `add` / `update` / `delete`** 四段
  - 注意与系统模块不同：`sdp-module-system` 用 `:list` / `:edit`，业务模块用 `:add` / `:update` / `:delete`
  - 列表与详情共用 `:list`
- 前端：页面组件 `<PageName>.vue` 放 `src/views/<moduleDir>/`；api 放 `src/api/generated/<varName>.ts`；路由 path `/<moduleDir>/<表名小写_转->`（kebab，如 `/order/buss-order-info`），动态菜单的 `component` 填 `<PageName>`
- 主键：Long → `@TableId(IdType.ASSIGN_ID)` + `@JsonSerialize(ToStringSerializer)`（防 JS 精度丢失）；String → `ASSIGN_UUID`

## 路径 A：在线代码生成器（首选）

入口：前端「系统管理 > 代码生成」（`SystemCodegen.vue`），或直接调 `POST /api/admin/codegen/create-table-and-generate`（需 `system:menu:edit` 权限）。

它做了什么（`AutoCodeAdminService` + `CodegenFacade`）：

1. 按字段拼**达梦** `CREATE TABLE`（表名/列名转大写；类型映射 `VARCHAR(n)`、`DECIMAL(p,s)`、`DATETIME→TIMESTAMP`、`TEXT→CLOB`；`PRIMARY KEY(...)`），用 `jdbcTemplate.execute` **真实建表**。
2. `CodegenFacade.generateCrud` 直接把 entity/mapper/service/controller **写入** `sdp-module-business/src/main/java/org/nmgyj/<pkg>/...`。
3. 生成前端 ZIP（`src/api/generated/*.ts` + `src/views/<moduleDir>/*.vue` + `README.md`），返回下载 `token`，经 `GET /api/admin/codegen/frontend-packages/{token}` 下载。

生成后**收尾步骤**（生成器不会自动做）：

```
新增模块收尾清单：
- [ ] 后端编译并重启：mvn -pl sdp-server -am -DskipTests compile，重启 sdp-server（新 Controller 才会被扫描）
- [ ] 前端 ZIP 解压到 front-end/sdp-app-vue（路由无需手改，走动态菜单注册）
- [ ] 系统管理>菜单 新增菜单：path=/<moduleDir>/<kebab>，component=<PageName>，perms=business:<表>:list
- [ ] 新增按钮资源（menuType=2）：business:<表>:add / :update / :delete
- [ ] 把菜单/按钮分配给目标角色（sys_role_menu），或用超管验证
- [ ] 校验中文文件 UTF-8 无 BOM（见 utf8-encoding skill）
- [ ] 验证：无权限用户 403、按钮隐藏；超管可见可用；列表/增改删通路
```

排错：页面空表格或 `No static resource api/admin/...` → 多半是后端没重启，新 Controller 未生效。

## 路径 B：手工新增（不走生成器时）

按上面命名约定手写，保持与生成器一致：

1. **建表 SQL**（达梦方言）：写进 `sdp-server/src/main/resources/db/schema-dm.sql`（种子数据进 `seed-dm.sql`）。表名/列名大写，类型用 `VARCHAR(n)`/`DECIMAL(p,s)`/`TIMESTAMP`/`CLOB`，显式 `PRIMARY KEY`。
2. **后端四件套**（包 `org.nmgyj.<pkg>`，落 `sdp-module-business`）：
   - `entity`：`@TableName("表名")`，主键 `@TableId(IdType.ASSIGN_ID)`，Long 字段加 `@JsonSerialize(using = ToStringSerializer.class)`，日期加 `@JsonFormat`+`@DateTimeFormat`，字段用 `@Schema` 注释。
   - `mapper`：继承 MyBatis-Plus `BaseMapper<Entity>`。
   - `service`：注入 mapper，提供 `listAll/getById/save/update/delete`。
   - `controller`：`@RequestMapping("/api/admin/<表名小写>")`，每个方法 `@SaCheckPermission("business:<表>:<op>")`，统一返回 `ApiResponse`，校验失败抛/转 `ApiResponse.fail`（异常交全局处理器，见 backend-logging）。
3. **前端**：`src/api/generated/<var>.ts`（用 `http`+`unwrap`）、`src/views/<moduleDir>/<PageName>.vue`（列表+弹窗 CRUD 模板，按钮加 `v-perm`）。详见 vue-page skill。
4. **路由/菜单/权限**：固定页面在 `router/index.ts` 注册，或走后台动态菜单；菜单 `perms` 与 controller 权限码逐字一致；分配给角色。详见 sa-token-rbac skill。

## 需要审计时

生成/手写的 controller 默认**不带审计**。若该业务的增删改需审计追踪（MES 关键动作如工单状态变更），在 service/controller 写方法上补 `@AuditLog(action=..., bizType="<表>", bizIdSpEL="#id", recordDiff=true)`，见 backend-logging skill。

## 修改代码生成器本身

若要调整生成产物（而非新增某个模块）：

- **只改模板**：`sdp-module-codegen/src/main/resources/codegen/templates/{backend,frontend}/*.th.txt`，不要在渲染器里写大段字符串模板。
- 新增模板变量时，先在 `CodegenFacade` 的 model 里补齐，再用 `[[${var}]]`。
- 变量约定见模板目录的 `TEMPLATE_GUIDE.md`。
- 改完至少跑 `CodegenFacadeTest` 验证核心生成路径。
