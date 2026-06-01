# SDP 工程化提升方案

> 目标：让 SDP 开发**高效、稳定、可维护**，向顶尖工业级项目的工程规范看齐。
> 本文档为架构与工程审查产物，包含现状盘点、差距优先级与分阶段落地路线。

## 成熟度快照

基于当前仓库实测（Maven 多模块后端 + Vue3 前端）。

| 维度 | 现状 | 评级 |
|------|------|------|
| 后端测试 | 仅 1 个 CodegenFacadeTest | 高风险 |
| 前端测试 / CI 流水线 | 0 | 高风险 |
| Lint / 格式化门禁 | 无 | 待补 |
| 数据库变更管理 | 手工 SQL | 待补 |
| 模块化分层 / 统一版本 | sdp-server/common/modules + revision + flatten | 已具备 |
| 三类日志 + traceId | 完善 | 已具备 |
| Sa-Token + RBAC | 完善 | 已具备 |
| 项目 Skills 规范资产 | 5 个 | 已具备 |

## 现状：已经做对的地方（不用动）

- **多模块架构**：`sdp-server/common/modules` 分层清晰，`dependencyManagement` + `revision` + flatten 统一版本。
- **技术栈现代**：Java 21 / Spring Boot 3.2.5，已预留 Spring Cloud Alibaba 依赖。
- **日志体系**：Access/Audit/Error 三类分流 + traceId 串联 + 脱敏 + 滚动归档。
- **认证鉴权**：Sa-Token + Redis Opaque Token，RBAC 用户-角色-菜单，`@SaCheckPermission` 落地。
- **统一契约**：`ApiResponse` + `GlobalExceptionHandler` + springdoc/OpenAPI。
- **代码生成器**：在线建表 + 全栈 CRUD 生成，模板化（Thymeleaf）可维护。
- **规范意识**：ADR 决策记录、设计文档、UTF-8 规范化脚本。
- **Skills 资产**：已沉淀 5 个项目 skill（日志/编码/鉴权/前端/新增模块）。

## 关键差距与优先级

> P0 = 地基/高风险，先做；P1 = 规范与稳态；P2 = 卓越与协作，持续推进。

| 维度 | 现状 | 影响 | 优先级 |
|------|------|------|:---:|
| 自动化测试 | 仅 1 个 CodegenFacadeTest；Service/Controller/Mapper、前端零测试 | 重构无保护网，回归靠手点，稳定性最大短板 | **P0** |
| CI/CD 门禁 | 无 GitHub Actions，构建/测试/编码检查不自动 | 坏代码可直接进主干，质量不可控 | **P0** |
| 数据库变更管理 | schema-dm.sql + codegen 直接 DDL，无 Flyway/Liquibase | 多环境结构漂移，不可重放/回滚，工业系统隐患 | **P0** |
| 静态检查 / 风格 | 后端无 Spotless/Checkstyle，前端无 ESLint/Prettier，无 .editorconfig | 风格不一致，低级 bug 漏网 | P1 |
| 配置与密钥外置 | dev yml 含明文连接配置，生产 profile 未收口 | 安全与合规风险 | P1 |
| 可观测性指标 | 有日志无 metrics/health；缺 Actuator + Micrometer + Prometheus | 上线后看不见系统状态，故障定位慢 | P1 |
| 安全基线收口 | CORS、登录限流/防爆破、密码策略、越权用例待补 | 工业系统安全底线 | P1 |
| 依赖与漏洞扫描 | 无 Dependabot / OWASP dependency-check / npm audit 门禁 | 供应链与已知漏洞风险 | P2 |
| Git 工作流 / 文档 | 无 Conventional Commits/PR 模板/CODEOWNERS；README 极简且有笔误 | 协作摩擦，新人上手慢 | P2 |
| 契约与错误码 | 有 springdoc 但无统一错误码字典 / API 版本策略 | 前后端协作摩擦，错误处理不一致 | P2 |

## 分阶段落地路线

### P0 · 地基 · 质量保护网（建议 1–2 周）

- **测试脚手架**：后端 JUnit5 + Mockito + spring-boot-test；为 `RbacPermissionService`、`AuditLogAspect`、各 Service 补单测；前端 Vitest + Vue Test Utils 起步。
- **CI 流水线**：GitHub Actions：`mvn verify` + 前端 build/test + 跑 `ensure-utf8` 编码检查；PR 必过门禁。
- **数据库迁移**：引入 Flyway（达梦用 `flyway-database-dameng`）或 Liquibase，把 schema/seed 纳入版本化迁移，codegen 改为产出迁移脚本。

### P1 · 规范与稳态（建议 3–4 周）

- **代码风格门禁**：后端 Spotless（google-java-format）+ Checkstyle；前端 ESLint + Prettier + lint script；加 `.editorconfig`。
- **配置 / 密钥外置**：敏感配置走环境变量/外部配置，生产禁用 dev profile，密码策略与 BCrypt 强制。
- **可观测性**：spring-boot-actuator + Micrometer + Prometheus 指标，健康检查，为微服务接 Tracing 铺路。
- **安全收口**：收紧 CORS、登录限流防爆破、越权与权限用例测试。

### P2 · 卓越与协作（持续）

- **供应链安全**：Dependabot + OWASP dependency-check + npm audit 纳入 CI。
- **协作规范**：Conventional Commits + PR/Issue 模板 + CODEOWNERS + 自动 changelog。
- **文档体系**：修订 README（含现有笔误）、快速启动、贡献指南、架构总览，补 `docs/adr/`。
- **契约与演进**：统一错误码字典 + API 版本策略；按规划推进 Spring Cloud Alibaba 微服务化。

## 立即可做的 Quick Wins

1. **加 `.editorconfig` + 前端 ESLint/Prettier**：统一缩进/换行/引号，立刻消除风格分歧；`package.json` 补 lint & type-check 脚本。
2. **修订 README.MD**：现有「sdp项目的前端目录：back-end」笔误需更正，补最小启动步骤（达梦 → Redis → sdp-server → 前端）。
3. **接入 Spotless 一次性格式化**：全仓库统一 Java 格式，后续 CI 校验，告别格式 diff 噪音。
4. **把 `ensure-utf8` 脚本接入 pre-commit / CI**：从源头拦截 Windows 下 UTF-16/BOM 乱码问题。
5. **关键业务动作补 `@AuditLog`**：codegen 产物默认无审计，对 MES 关键动作（状态变更等）按规范补齐。

## 建议起点

先打 P0 三件套（测试脚手架 + CI + 数据库迁移）——它们是后续所有改进的安全网与门禁。

其中**数据库迁移**对达梦需用 `flyway-database-dameng` 或 Liquibase 验证兼容性，建议第一步先做小范围 PoC。

---

> 配套规范见 `.cursor/skills/` 下的项目 skill：`backend-logging`、`utf8-encoding`、`sa-token-rbac`、`vue-page`、`add-crud-module`。
