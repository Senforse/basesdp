# 前端页面生成包（[[${pageName}]])

本压缩包由系统自动生成，对应后端表 `[[${tableApiPath}]]`。

## 包内目录

- `src/api/generated/[[${varName}]].ts`
- `src/views/[[${moduleDir}]][[${moduleDir.endsWith('/') ? '' : '/'}]][[${pageName}]].vue`

## 使用步骤

1. 先在后端工程执行 `mvn -pl sdp-server -am -DskipTests compile`，并重启 `sdp-server`（确保新生成的 Controller 被 Spring 扫描加载）。
2. 将压缩包解压到前端项目根目录（`front-end/sdp-app-vue`）。
3. 在系统菜单中新增并分配该页面菜单，推荐配置：
   - `path`: `[[${routePath}]]`
   - `component`: `[[${dynamicRouteKey}]]`
   - `perms`: `[[${permPrefix}]]:list`
4. 若启用操作级权限，请在菜单管理中为该页面新增按钮资源（menuType=2）并分配到角色：
   - `[[${permPrefix}]]:add`
   - `[[${permPrefix}]]:update`
   - `[[${permPrefix}]]:delete`
5. 页面默认访问后端接口：`/api/admin/[[${tableApiPath}]]`。
6. 运行 `npm run build` 或 `npm run dev` 验证。

## 注意事项

- 页面已按字段自动生成列表和表单。
- 前端采用“后端菜单动态注册路由”方案，无需再手工修改 `src/router/index.ts`。
- 若后端菜单来自数据库，请将菜单 component 配置为 `[[${dynamicRouteKey}]]`，并确保页面文件名与 component 保持一致（如 `BussOrderInfo.vue`）。
- 若页面只显示空表格或接口报 `No static resource api/admin/...`，通常是后端未重启导致新 Controller 未生效。

## 本次生成 SQL

```sql
[[${createTableSql}]]
```
