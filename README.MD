# SDP（Smart Development Platform）

股份公司自研的企业数字化 / 工业软件开发平台。目标：高效落地企业应用、逐步摆脱外部工业软件供应商依赖，并演进为**可接入私有化 AI** 的工业级开发平台（详见 [docs/ai-platform-vision.md](docs/ai-platform-vision.md)）。

> **给 AI / 新协作者**：先读本文「一分钟认知」与「文档索引」，再按需打开 `docs/` 与 `.cursor/skills/`，避免每次从零扫仓库。

---

## 一分钟认知

| 项 | 说明 |
|----|------|
| **形态** | 当前为 **Modular Monolith**（`sdp-server` 单进程启动）；`sdp-api` / `sdp-gateway` 为预留空壳，**业务做深前勿急于拆微服务** |
| **后端** | `back-end/sdp-admin` · Java 21 · Spring Boot 3.2.5 · MyBatis-Plus · **达梦 DM** · Druid |
| **前端** | `front-end/sdp-app-vue` · Vue 3 · TS · Vite · Pinia · Element Plus |
| **认证** | **Sa-Token + Redis**（`satoken` 请求头）；RBAC 用户-角色-菜单-权限；服务端 `@SaCheckPermission` 为准 |
| **契约** | 统一 `ApiResponse` + `GlobalExceptionHandler` + springdoc/OpenAPI |
| **平台能力** | 在线代码生成器（表元数据 → 全栈 CRUD）、三类日志（Access/Audit/Error）+ traceId |
| **战略节奏** | 工程地基(P0) → 平台元数据 → **A 档 AI**（NL→模块）→ 业务数据 → B 档 AI；**平台从首个工业场景「长出来」**，非先做「超大全功能平台」 |

---

## 仓库目录

```
basesdp/
├── back-end/sdp-admin/          # Maven 多模块后端（唯一启动：sdp-server）
├── front-end/sdp-app-vue/       # 管理端前端
├── docs/                        # 愿景、工程路线图、ADR 等
├── .cursor/skills/              # Cursor Agent 项目规范（增模块/日志/RBAC/前端/UTF-8）
└── README.MD                    # 本文件（项目总览入口）
```

### 后端模块（`back-end/sdp-admin`）

```
sdp-admin (pom, revision + flatten 统一版本)
├── sdp-server              # 启动入口 SdpServerApplication，默认 8080
├── sdp-common              # sdp-common-core / security / mybatis（多为依赖聚合）
├── sdp-modules
│   ├── sdp-module-system   # 认证、RBAC、字典、组织、日志、在线用户、codegen 元数据 API 等
│   ├── sdp-module-codegen  # Thymeleaf 模板，生成 entity/mapper/service/controller + 前端 api/vue
│   └── sdp-module-business # 业务域（示例：order/BussOrderInfo；生产相关页面在前端已有占位）
├── sdp-api                 # 【空壳】Feign/DTO 契约预留
└── sdp-gateway             # 【空壳】网关预留
```

**包约定（已落地代码）**

- 系统 / 认证：`org.nmgyj.authentication.*`（登录、拦截器、审计切面、访问日志）
- 系统管理：`org.nmgyj.system.*`（用户、角色、菜单、组织、字典等）
- 代码生成：`org.nmgyj.codegen.*`
- 业务：`org.nmgyj.<domain>.*`（如 `org.nmgyj.order`）

### 前端结构（`front-end/sdp-app-vue`）

- `src/api/` — axios 封装，`http.ts` 解包 `ApiResponse`，请求头带 `satoken`
- `src/views/` — 页面（含 `system/`、`production/` 等）
- `src/directives/permission.ts` — `v-perm` 按钮级权限
- 开发代理：`/api` → `http://localhost:8080`

---

## 技术栈与版本

| 层 | 选型 |
|----|------|
| 语言 / 运行时 | Java 21 |
| 框架 | Spring Boot 3.2.5（已锁 Spring Cloud / Alibaba 版本，未全面启用） |
| ORM | MyBatis-Plus 3.5.8，`DbType.DM` 分页 |
| 数据库 | 达梦 8（JDBC `DmJdbcDriver18`） |
| 缓存 / 会话 | Redis（Sa-Token）；Caffeine 本地缓存 |
| 鉴权 | Sa-Token 1.44.0 |
| API 文档 | springdoc 2.5 |
| 前端 | Vue 3.5 + Vite 8 + Element Plus 2.x + TS |

**源文件编码**：全仓库 **UTF-8 无 BOM**（Windows 下改含中文文件后注意校验，见 `.cursor/skills/utf8-encoding`）。

---

## 本地启动（最小路径）

**依赖**：达梦（已建库/schema）、Redis（默认 `127.0.0.1:6379`）、JDK 21、Maven、Node.js。

1. **数据库**：执行 `back-end/sdp-admin/sdp-server/src/main/resources/db/schema-dm.sql` 及 `sdp-modules/sdp-module-system/src/main/resources/db/rbac-dm.sql`（环境差异以实际脚本为准）。
2. **配置**：`sdp-server/src/main/resources/application-dev.yml` 配置数据源与 Redis（**勿将生产密码提交仓库**；敏感项应外置环境变量，见工程路线图 P1）。
3. **后端**：
   ```bash
   cd back-end/sdp-admin
   mvn -pl sdp-server -am spring-boot:run
   ```
   默认 `http://localhost:8080`。
4. **前端**：
   ```bash
   cd front-end/sdp-app-vue
   npm install
   npm run dev
   ```
   Vite 默认端口见终端输出；接口经 `/api` 代理到 8080。

**构建**：后端 `mvn verify`（根目录在 `sdp-admin`）；前端 `npm run build`。

---

## 已具备 vs 关键缺口

### 已具备（可依赖）

- 多模块分层、`dependencyManagement` + `${revision}` + flatten
- Sa-Token + RBAC + 动态菜单路由
- Access / Audit / Error 日志分流 + traceId + 脱敏 + 归档配置
- 在线代码生成器（元数据表 → 全栈代码）
- 5 个项目 Skill（见下）
- 愿景与工程化文档（`docs/`）

### 关键缺口（上 AI / 大规模迭代前优先补，P0）

| 缺口 | 影响 |
|------|------|
| 自动化测试极少（后端约 1 个 codegen 测试，前端无测试） | AI/人工改代码无回归保护 |
| 无 CI 门禁 | 质量不可控 |
| DB 变更靠手工 SQL，无 Flyway/Liquibase | 多环境漂移 |
| `sdp-api` / `sdp-gateway` 未启用 | 正常，勿为拆而拆 |

详细优先级与阶段任务 → [docs/engineering-roadmap.md](docs/engineering-roadmap.md)。

---

## 产品与 AI 方向（摘要）

完整共识见 [docs/ai-platform-vision.md](docs/ai-platform-vision.md)。核心结论：

1. **行业**：离散制造；推荐首场景 **生产工单/执行 + 质量追溯**（同场景闭环，为后续 B 档 AI 攒数据）。
2. **AI 顺序**：A 档（NL → 数据模型 → 现有生成器 → 全栈模块；RAG 问答/排错）→ 有数据后再 B 档（质检预测等）→ C 档（排产/闭环控制）**现阶段不碰**。
3. **私有化**：数据不出网；Ollama 验证 → vLLM；Java 侧计划 **Spring AI** + OpenAI 兼容端点；向量检索 **Milvus/Qdrant**，不依赖达梦向量类型。
4. **工业 AI 原则**：可解释、可追溯、人在回路；**审计日志（before/after + traceId）是差异化优势**。

---

## 文档索引（按阅读顺序）

| 文档 | 用途 |
|------|------|
| [README.MD](README.MD) | 本总览 |
| [docs/ai-platform-vision.md](docs/ai-platform-vision.md) | AI 工业平台愿景、场景、AI 分层、私有化选型（结论） |
| [docs/ai-platform-discussion-log.md](docs/ai-platform-discussion-log.md) | 上述方向的**讨论与思考过程**（推导/备选/判断依据，防上下文丢失） |
| [docs/engineering-roadmap.md](docs/engineering-roadmap.md) | 工程成熟度、P0/P1/P2、Quick Wins |
| [back-end/sdp-admin/README.md](back-end/sdp-admin/README.md) | 后端模块分层设计说明（含规划中的多租户等） |
| [back-end/sdp-admin/DESIGN_UTF8.md](back-end/sdp-admin/DESIGN_UTF8.md) | 管理端设计方案（认证、RBAC、达梦、API 约定） |
| [back-end/sdp-admin/docs/sdp-auth-rbac-design.md](back-end/sdp-admin/docs/sdp-auth-rbac-design.md) | RBAC 表结构与权限模型 |
| [back-end/sdp-admin/ADR.MD](back-end/sdp-admin/ADR.MD) | 架构决策记录 |
| [front-end/sdp-app-vue/vue-adr.md](front-end/sdp-app-vue/vue-adr.md) | 前端 ADR（样式注入等） |

---

## Cursor Agent 规范（`.cursor/skills/`）

执行任务前优先加载对应 Skill，保证与仓库约定一致：

| Skill | 何时用 |
|-------|--------|
| `add-crud-module` | 新增业务表 + 后端 + 前端 + 路由菜单 + RBAC 权限（端到端） |
| `backend-logging` | Controller/Service 日志、审计 `@AuditLog`、异常与 traceId |
| `sa-token-rbac` | 接口权限、`@SaCheckPermission`、权限点与 `v-perm` |
| `vue-page` | `src/views` / `src/api`、列表弹窗 CRUD、动态路由 |
| `utf8-encoding` | Windows 编码/BOM/乱码修复 |

**新增 CRUD 推荐路径**：在线代码生成器产出骨架 → 按 Skill 补权限、审计、菜单、联调。

---

## 常见开发约定

- **API 前缀**：`/api/...`；登录等见 `AuthController`。
- **权限**：后端注解为准；前端 `v-perm` 仅控制可见性。
- **响应体**：`{ code, message, data }`，前端在 `http.ts` 统一解包。
- **新业务模块**：放在 `sdp-module-business` 或新增 `sdp-module-*`，避免堆在 system 模块。
- **日志**：接口访问自动记 Access；业务写操作关键路径用 Audit；异常走 Error。
- **Codegen**：模板在 `sdp-module-codegen/src/main/resources/codegen/templates/`。
- **提交前**：在 `sdp-admin` 跑 `mvn -pl sdp-server -am test`；前端 `npm run build`（CI 落地前仍靠本地）。

---

## 给 AI 的协作提示

1. **不要假设微服务已启用**——默认只改 `sdp-server` 聚合的模块。
2. **改含中文的文件后**留意 UTF-8（必要时跑 `back-end/sdp-admin/normalize_text_encoding.py` 或 `ensure-utf8` 类脚本）。
3. **大方向变更**先对齐 `docs/ai-platform-vision.md`，**工程门禁**对齐 `docs/engineering-roadmap.md`。
4. **工业场景代码**优先补 `@AuditLog` 与权限点，再追求功能广度。
5. **README 本身**：若架构/缺口/文档路径有变，应同步更新本节与「文档索引」，保持「一次读取即可上手」。

---

## 许可证与归属

股份公司自研内部项目；对外发布与许可证以公司规定为准。
