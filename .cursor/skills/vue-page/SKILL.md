---
name: vue-page
description: SDP 前端（sdp-app-vue，Vue3 + TS + Vite + Pinia + Element Plus）新增/修改管理页面的约定，包括 api 层封装、ApiResponse 解包、http 拦截器、Element Plus 列表+弹窗 CRUD 结构、路由与动态菜单、v-perm 权限、SCSS 主题类名。在编写 src/views 下页面、src/api 接口、store 或路由时使用。
---

# SDP 前端页面约定

技术栈：Vue 3 `<script setup lang="ts">` + TypeScript + Vite + Pinia + Vue Router + Element Plus + SCSS。目录 `front-end/sdp-app-vue/`。约定见 `front-end/sdp-app-vue/vue-adr.md`。

## API 层（src/api）

**所有请求走 `src/api/http.ts` 的默认 axios 实例**，禁止页面里裸用 axios/fetch。

- `http` 实例自动注入 token 头：从 `localStorage` 读 `token` 与 `tokenHeaderName`（默认 `satoken`）。401 自动清登录态并跳 `/login`。
- 后端统一响应 `ApiEnvelope<T> = {code,message,data}`（见 `src/api/types.ts`），`code===0` 为成功。
- **用 `unwrap()` 解包**，失败抛 `Error(message)`，由页面 catch 提示。

接口函数集中放 `src/api/*.ts`（如 `admin.ts`），每个函数一行 JSDoc 标 HTTP 方法+路径，签名返回业务类型：

```ts
import http, { unwrap } from './http'
import type { ApiEnvelope, SysRole } from './types'

/** GET /api/admin/roles */
export async function fetchRoles(): Promise<SysRole[]> {
  return unwrap(await http.get<ApiEnvelope<SysRole[]>>('/api/admin/roles'))
}

/** POST /api/admin/roles */
export async function createRole(body: SysRole): Promise<void> {
  unwrap(await http.post<ApiEnvelope<null>>('/api/admin/roles', body))
}
```

- 类型定义统一进 `src/api/types.ts`，与后端实体字段一致并注释"与后端 Xxx 一致"。
- 代码生成的接口放 `src/api/generated/`，不要手改生成文件。

## 页面结构（src/views）

标准"列表 + 弹窗 CRUD"模板，参考 `src/views/system/SystemRole.vue`：

- `<script setup lang="ts">`：`ref/reactive` 管状态；`load()` 拉列表，`onMounted(load)`；`openCreate/openEdit/submit/remove` 配 `el-dialog`。
- 所有异步操作 `try/catch`，成功用 `ElMessage.success`，失败 `ElMessage.error(e instanceof Error ? e.message : '操作失败')`。
- 删除前用 `ElMessageBox.confirm`，捕获 `'cancel'` 时静默返回。
- 表格 `<el-table v-loading="loading" border row-key="id">`；表单编辑禁改主键/编码（`:disabled="editingId != null"`）。

根节点固定类名（见 `assets/styles`）：

```vue
<template>
  <div class="sdp-page">
    <el-card class="sdp-panel" shadow="never">
      <template #header>
        <div class="sdp-panel-head">
          <span class="sdp-panel-title">页面标题</span>
          <el-button type="primary" v-perm="'模块:资源:edit'" @click="openCreate">新增</el-button>
        </div>
      </template>
      <!-- el-table ... -->
    </el-card>
  </div>
</template>
```

样式用 `<style lang="scss" scoped>`，颜色/间距优先用 `assets/styles/variables.scss`、`themes.scss` 的变量，勿硬编码主题色。

## 路由与菜单

`src/router/index.ts`：固定页面写在 `staticRoutes`（`BasicLayout` 作为壳 + 子路由懒加载 `() => import(...)`）。后台菜单驱动的页面由 `registerDynamicRoutesFromMenu()` 依据 `useMenuStore().flatRoutes` 动态注册——**新增受菜单控制的页面只需放好组件并配菜单数据，无需手改静态路由**。

`router.beforeEach` 守卫：无 token → `/login`；首次进入加载 `menuStore` 与 `permissionStore`。

新增系统管理类页面：在 `views/system/` 建组件 → `staticRoutes` 的 `/system` children 加一项（或走动态菜单）→ 后端菜单/权限配齐。

## 按钮级权限

用 `v-perm` 指令（`src/directives/permission.ts`，注册名 `perm`），权限码取自 `usePermissionStore`：

```vue
<el-button v-perm="'system:role:edit'">新增</el-button>
<el-button v-perm="['system:role:edit','system:role:assign']">操作</el-button>
```

字符串=需该权限；数组=任一即可；持 `*` 全通过。前端权限码必须与后端 `@SaCheckPermission` 逐字一致（详见 sa-token-rbac skill）。

## 注意

- 中文文案文件保存为 UTF-8 无 BOM（见 utf8-encoding skill）。
- Element Plus 组件需确保 `package.json` 已声明 `element-plus` 依赖再使用。
